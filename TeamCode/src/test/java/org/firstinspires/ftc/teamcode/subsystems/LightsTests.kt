package org.firstinspires.ftc.teamcode.subsystems

import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.BLUE
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.GREEN
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.ORANGE
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.RED
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.Color.WHITE
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.GoBildaPrismDriver.Artboard.ARTBOARD_0
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.GoBildaPrismDriver.LayerHeight.LAYER_0
import org.firstinspires.ftc.teamcode.adaptations.gobilda.prism.PrismAnimations
import org.firstinspires.ftc.teamcode.game.Alliance
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class LightsTests : SubsystemTests() {
    lateinit var timer: ElapsedTime

    @Before
    fun setUp() {
        Lights.initializeHardware()
        timer = mock(ElapsedTime::class.java)
        Timing.playTimer = timer
        Lights.color = RED
        Intake.full = false
        state.started = false
        state.teleop = false
        config.alliance = Alliance.UNKNOWN
        clearInvocations(Lights.prism)
    }

    @After
    fun resetTimer() {
        Timing.playTimer = ElapsedTime()
    }

    @Test
    fun explicitCommandsAndDuplicateSuppressionSetColors() {
        Lights.set(BLUE).start()
        assertEquals(BLUE, Lights.color)
        val animation = ArgumentCaptor.forClass(PrismAnimations.Solid::class.java)
        verify(Lights.prism).insertAndUpdateAnimation(eq(LAYER_0), animation.capture())
        assertEquals(BLUE, animation.value.primaryColor)

        clearInvocations(Lights.prism)
        Lights.show(BLUE)
        verify(Lights.prism, never()).insertAndUpdateAnimation(
            eq(LAYER_0),
            any(PrismAnimations.AnimationBase::class.java),
        )
    }

    @Test
    fun configuresVendorDriver() {
        Lights.configure(Lights.prism)

        verify(Lights.prism).setStripLength(48)
        val animation = ArgumentCaptor.forClass(PrismAnimations.Solid::class.java)
        verify(Lights.prism).insertAndUpdateAnimation(eq(LAYER_0), animation.capture())
        assertEquals(RED, animation.value.primaryColor)
        verify(Lights.prism).saveCurrentAnimationsToArtboard(ARTBOARD_0)
        verify(Lights.prism).setDefaultBootArtboard(ARTBOARD_0)
        verify(Lights.prism).enableDefaultBootArtboard(true)
    }

    @Test
    fun intakeFullTakesDisplayPriority() {
        Intake.full = true

        Lights.periodic()

        assertEquals(GREEN, Lights.color)
    }

    @Test
    fun matchTimeSelectsWarningColors() {
        state.started = true
        state.teleop = true

        `when`(timer.seconds()).thenReturn(80.0)
        config.alliance = Alliance.BLUE
        Lights.periodic()
        assertEquals(BLUE, Lights.color)

        `when`(timer.seconds()).thenReturn(85.0)
        Lights.periodic()
        assertEquals(WHITE, Lights.color)

        `when`(timer.seconds()).thenReturn(105.0)
        Lights.periodic()
        assertEquals(ORANGE, Lights.color)

        `when`(timer.seconds()).thenReturn(115.0)
        Lights.periodic()
        assertEquals(RED, Lights.color)
    }

    @Test
    fun allianceSelectsIdleColor() {
        config.alliance = Alliance.BLUE
        Lights.periodic()
        assertEquals(BLUE, Lights.color)

        config.alliance = Alliance.RED
        Lights.periodic()
        assertEquals(RED, Lights.color)

        config.alliance = Alliance.UNKNOWN
        Lights.periodic()
        assertEquals(RED, Lights.color)
    }

    @Test
    fun matchWarningsRequireBothStartedAndTeleop() {
        `when`(timer.seconds()).thenReturn(120.0)
        config.alliance = Alliance.BLUE

        state.started = false
        state.teleop = true
        Lights.periodic()
        assertEquals(BLUE, Lights.color)

        state.started = true
        state.teleop = false
        Lights.periodic()
        assertEquals(BLUE, Lights.color)
    }

    @Test
    fun configurableValuesRemainMutable() {
        Lights.stripLength = Lights.stripLength
    }
}
