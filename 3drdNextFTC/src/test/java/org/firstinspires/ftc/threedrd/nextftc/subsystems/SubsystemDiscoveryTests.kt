package org.firstinspires.ftc.threedrd.nextftc.subsystems

import dalvik.system.DexFile
import java.util.Collections
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.AbstractSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.ConstructedSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.DiscoveredSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.InvalidInstanceSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.NestedSubsystem
import org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures.NotASubsystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SubsystemDiscoveryTests {
    @Test
    fun discoversTopLevelSingletonSubsystemsRegardlessOfPackage() {
        val discovered = SubsystemDiscovery.discover(
            listOf(
                String::class.java.name,
                NotASubsystem::class.java.name,
                AbstractSubsystem::class.java.name,
                NestedSubsystem.Instance::class.java.name,
                DiscoveredSubsystem::class.java.name
            ),
            javaClass.classLoader
        )

        assertEquals(setOf(DiscoveredSubsystem), discovered)
    }

    @Test
    fun usesTheSubsystemClassLoaderByDefault() {
        assertEquals(
            setOf(DiscoveredSubsystem),
            SubsystemDiscovery.discover(listOf(DiscoveredSubsystem::class.java.name))
        )
    }

    @Test
    fun skipsClassesUnavailableOnTheRobotRuntime() {
        val failures = mapOf(
            "missing.Direct" to ClassNotFoundException("missing.Direct"),
            "missing.Dependency" to NoClassDefFoundError("missing.Dependency"),
            "missing.Wrapped" to RuntimeException(
                "missing.Wrapped",
                ClassNotFoundException("missing.Wrapped")
            )
        )
        val classLoader = object : ClassLoader(javaClass.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                failures[name]?.let { throw it }
                return super.loadClass(name, resolve)
            }
        }

        assertEquals(
            setOf(DiscoveredSubsystem),
            SubsystemDiscovery.discover(
                failures.keys + DiscoveredSubsystem::class.java.name,
                classLoader
            )
        )
    }

    @Test
    fun preservesUnrelatedClassLoadingFailures() {
        val failure = IllegalStateException("broken loader")
        val classLoader = object : ClassLoader(javaClass.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> = throw failure
        }

        val thrown = runCatching {
            SubsystemDiscovery.discover(listOf("broken.Class"), classLoader)
        }.exceptionOrNull()

        assertSame(failure, thrown)
    }

    @Test
    fun rejectsConstructedSubsystems() {
        val exception = runCatching {
            SubsystemDiscovery.discover(
                listOf(ConstructedSubsystem::class.java.name),
                javaClass.classLoader
            )
        }.exceptionOrNull()

        assertEquals(
            "${ConstructedSubsystem::class.java.name} must be a Kotlin object",
            exception?.message
        )
    }

    @Test
    fun rejectsAnInvalidInstanceField() {
        val exception = runCatching {
            SubsystemDiscovery.discover(
                listOf(InvalidInstanceSubsystem::class.java.name),
                javaClass.classLoader
            )
        }.exceptionOrNull()

        assertEquals(
            "${InvalidInstanceSubsystem::class.java.name} must be a Kotlin object",
            exception?.message
        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun readsClassNamesFromTheRobotControllerApk() {
        mockConstruction(DexFile::class.java) { dex, context ->
            assertEquals("robot.apk", context.arguments()[0])
            `when`(dex.entries()).thenReturn(
                Collections.enumeration(listOf("Deflector", "Gate"))
            )
        }.use { dexFiles ->
            assertEquals(
                listOf("Deflector", "Gate"),
                SubsystemDiscovery.classNames("robot.apk")
            )
            assertEquals(1, dexFiles.constructed().size)
            verify(dexFiles.constructed().single()).close()
        }
    }
}
