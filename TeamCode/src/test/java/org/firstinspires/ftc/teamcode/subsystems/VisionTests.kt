package org.firstinspires.ftc.teamcode.subsystems

import com.pedropathing.follower.Follower
import com.pedropathing.geometry.Pose
import com.qualcomm.hardware.limelightvision.LLResult
import com.qualcomm.hardware.limelightvision.LLResultTypes
import com.qualcomm.robotcore.util.ElapsedTime
import dev.nextftc.extensions.pedro.PedroComponent
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.util.Base64
import java.util.zip.GZIPOutputStream
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS
import org.firstinspires.ftc.robotcore.external.navigation.Position
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.ConfigComponent
import org.firstinspires.ftc.teamcode.adaptations.quanomous.Quanomous as QuanomousData
import org.firstinspires.ftc.teamcode.adaptations.quanomous.QuanomousStorage
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.APRIL_TAG
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.GREEN
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.PURPLE
import org.firstinspires.ftc.teamcode.adaptations.vision.Pipeline.QR_CODE
import org.firstinspires.ftc.teamcode.game.Alliance.BLUE
import org.firstinspires.ftc.teamcode.game.Alliance.RED
import org.firstinspires.ftc.teamcode.subsystems.Config.config
import org.firstinspires.ftc.teamcode.subsystems.Config.state
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class VisionTests : SubsystemTests() {
    lateinit var pedro: PedroComponent
    lateinit var follower: Follower
    lateinit var timer: ElapsedTime

    @Before
    fun setUp() {
        follower = mock(Follower::class.java)
        `when`(follower.pose).thenReturn(Pose())
        pedro = PedroComponent { follower }.apply { preInit() }
        Vision.initializeHardware()
        timer = mock(ElapsedTime::class.java)
        Vision.timer = timer
        Vision.initialize()
        `when`(Vision.limelight.isConnected).thenReturn(true)
        `when`(timer.seconds()).thenReturn(1.0)
        config.alliance = BLUE
        config.quanomous = null
        state.started = false
        state.teleop = false
        ConfigComponent.onChange = {}
        QuanomousData.storage = QuanomousStorage(Files.createTempDirectory("vision-quanomous").toFile())
        QuanomousData.decoder = { Base64.getDecoder().decode(it) }
        QuanomousData.lastHash = null
        QuanomousData.lastName = null
        clearInvocations(Vision.limelight)
    }

    @After
    fun tearDown() {
        pedro.postStop()
        Vision.timer = ElapsedTime()
        QuanomousData.storage = QuanomousStorage()
        QuanomousData.decoder = { android.util.Base64.decode(it, android.util.Base64.NO_WRAP) }
        QuanomousData.lastHash = null
        QuanomousData.lastName = null
    }

    @Test
    fun lifecycleInitializesAndStopsVisionHardware() {
        assertEquals(QR_CODE, Vision.pipeline)
        assertNull(Vision.result)
        assertNull(Vision.botpose)
        assertNull(Vision.element)
        verify(timer).reset()

        Vision.stop()
        verify(Vision.limelight).stop()
    }

    @Test
    fun periodicReportsDisconnectedCameraAndSkipsProcessing() {
        `when`(Vision.limelight.isConnected).thenReturn(false)
        clearInvocations(Vision.limelight)

        Vision.periodic()

        verify(Vision.limelight, never()).latestResult
    }

    @Test
    fun periodicUpdatesOrientationServoAndDispatchesEveryPipeline() {
        val result = mock(LLResult::class.java)
        `when`(result.isValid).thenReturn(true)
        `when`(result.barcodeResults).thenReturn(emptyList())
        `when`(result.fiducialResults).thenReturn(emptyList())
        `when`(result.colorResults).thenReturn(emptyList())
        `when`(Vision.limelight.latestResult).thenReturn(result)

        for (pipeline in listOf(QR_CODE, APRIL_TAG, GREEN, PURPLE)) {
            Vision.pipeline = pipeline
            Vision.periodic()
        }

        verify(Vision.limelight, org.mockito.Mockito.times(4)).updateRobotOrientation(0.0)
        assertEquals(result, Vision.result)

        `when`(timer.seconds()).thenReturn(0.0)
        Vision.periodic()
        `when`(result.isValid).thenReturn(false)
        `when`(timer.seconds()).thenReturn(1.0)
        Vision.periodic()
        `when`(Vision.limelight.latestResult).thenReturn(null)
        Vision.botpose = Pose(1.0, 2.0, 3.0)
        Vision.element = Pose(4.0, 5.0)
        Vision.periodic()
    }

    @Test
    fun periodicDisplaysDetectedRobotAndElementPoses() {
        val tagResult = mock(LLResult::class.java)
        val fiducial = mock(LLResultTypes.FiducialResult::class.java)
        val pose = mock(Pose3D::class.java)
        val orientation = mock(YawPitchRollAngles::class.java)
        `when`(tagResult.isValid).thenReturn(true)
        `when`(tagResult.fiducialResults).thenReturn(listOf(fiducial))
        `when`(fiducial.fiducialId).thenReturn(20)
        `when`(pose.position).thenReturn(Position(null, 1.0, 2.0, 3.0, 0))
        `when`(pose.orientation).thenReturn(orientation)
        `when`(tagResult.botpose_MT2).thenReturn(pose)
        `when`(Vision.limelight.latestResult).thenReturn(tagResult)
        Vision.pipeline = APRIL_TAG

        Vision.periodic()
        assertTrue(Vision.botpose != null)

        val colorResult = mock(LLResult::class.java)
        val color = mock(LLResultTypes.ColorResult::class.java)
        `when`(colorResult.isValid).thenReturn(true)
        `when`(colorResult.colorResults).thenReturn(listOf(color))
        `when`(color.targetYDegrees).thenReturn(-20.0)
        `when`(Vision.limelight.latestResult).thenReturn(colorResult)
        state.started = true
        Vision.pipeline = GREEN

        Vision.periodic()
        assertTrue(Vision.element != null)
    }

    @Test
    fun lockModesAndPipelineSwitchingManageTurretAndElements() {
        Vision.goalLock(false)
        Vision.goalLock(true)
        assertEquals(QR_CODE, Vision.pipeline)

        state.teleop = true
        Vision.goalLock.start()
        assertEquals(APRIL_TAG, Vision.pipeline)
        assertEquals(Vision.POS_GOAL_LOCK, Vision.POS, 0.0)
        Vision.goalUnlock.start()
        assertEquals(APRIL_TAG, Vision.pipeline)

        Vision.element = Pose(1.0, 1.0)
        Vision.purpleArtifacts += Vision.element!!
        Vision.chaseLock.start()
        assertEquals(PURPLE, Vision.pipeline)
        assertNull(Vision.element)
        assertEquals(Vision.POS_CHASE_LOCK, Vision.POS, 0.0)
        Vision.chaseUnlock.start()

        Vision.switchPipeline(GREEN)
        assertEquals(Vision.POS_CHASE_LOCK, Vision.POS, 0.0)
        verify(Vision.limelight).pipelineSwitch(GREEN.index)
    }

    @Test
    fun resetAndBackupElementManageSelection() {
        Vision.resetElement()
        Vision.setBackupElement()
        val backup = Vision.element
        assertTrue(backup != null)
        Vision.setBackupElement()
        assertEquals(backup, Vision.element)

        Vision.purpleArtifacts += listOf(backup!!, Pose(100.0, 100.0))
        Vision.greenArtifacts += backup
        Vision.greenArtifacts += Pose(100.0, 100.0)
        Vision.reset.start()

        assertNull(Vision.element)
        assertEquals(1, Vision.purpleArtifacts.size)
        assertEquals(1, Vision.greenArtifacts.size)

        val wait = Vision.waitForElement()
        assertFalse(wait.isDone)
        Vision.element = Pose()
        assertTrue(wait.isDone)
    }

    @Test
    fun qrCodesStoreAndSelectQuanomousPrograms() {
        val barcode = mock(LLResultTypes.BarcodeResult::class.java)
        val result = mock(LLResult::class.java)
        val json = "[{\"cmd\":\"score\"}]"
        val encoded = ByteArrayOutputStream().use { output ->
            GZIPOutputStream(output).use { it.write(json.toByteArray()) }
            Base64.getEncoder().encodeToString(output.toByteArray())
        }
        `when`(barcode.data).thenReturn(encoded)
        `when`(barcode.family).thenReturn("QR")
        `when`(result.barcodeResults).thenReturn(listOf(barcode))
        var changes = 0
        ConfigComponent.onChange = { changes++ }

        Vision.processQrCode(result)

        assertEquals(1, changes)
        assertEquals(QuanomousData.options().single(), config.quanomous)
    }

    @Test
    fun aprilTagsAcceptOnlyTheAllianceGoalTag() {
        val result = mock(LLResult::class.java)
        val fiducial = mock(LLResultTypes.FiducialResult::class.java)
        `when`(result.fiducialResults).thenReturn(listOf(fiducial))
        `when`(fiducial.fiducialId).thenReturn(19)
        Vision.processAprilTag(result)
        assertNull(Vision.botpose)

        `when`(fiducial.fiducialId).thenReturn(20)
        val pose = mock(Pose3D::class.java)
        val position = Position(null, 1.0, 2.0, 3.0, 0)
        val orientation = mock(YawPitchRollAngles::class.java)
        `when`(orientation.getYaw(RADIANS)).thenReturn(0.5)
        `when`(pose.position).thenReturn(position)
        `when`(pose.orientation).thenReturn(orientation)
        `when`(result.botpose_MT2).thenReturn(pose)
        Vision.processAprilTag(result)
        assertEquals(Vision.INCHES_PER_METER, Vision.botpose!!.x, 0.0)
        assertEquals(2 * Vision.INCHES_PER_METER, Vision.botpose!!.y, 0.0)

        config.alliance = RED
        Vision.botpose = null
        `when`(fiducial.fiducialId).thenReturn(20)
        Vision.processAprilTag(result)
        assertNull(Vision.botpose)
        `when`(fiducial.fiducialId).thenReturn(24)
        Vision.processAprilTag(result)
        assertTrue(Vision.botpose != null)

        `when`(result.fiducialResults).thenReturn(emptyList())
        Vision.botpose = null
        Vision.processAprilTag(result)
        assertNull(Vision.botpose)
    }

    @Test
    fun colorResultsProjectAndSelectNearbyArtifacts() {
        val result = mock(LLResult::class.java)
        val color = mock(LLResultTypes.ColorResult::class.java)
        `when`(color.targetXDegrees).thenReturn(2.0)
        `when`(color.targetYDegrees).thenReturn(-20.0)
        `when`(result.colorResults).thenReturn(listOf(color))

        Vision.processColor(result, Vision.greenArtifacts, Vision.purpleArtifacts)
        assertTrue(Vision.greenArtifacts.isEmpty())

        state.started = true
        Vision.CAMERA_UPSIDE_DOWN = true
        Vision.greenArtifacts += Pose(100.0, 100.0)
        Vision.greenArtifacts += Vision.elementPose(2.0, -20.0)
        Vision.processColor(result, Vision.greenArtifacts, Vision.purpleArtifacts)
        val first = Vision.element
        assertTrue(first != null)

        Vision.CAMERA_UPSIDE_DOWN = false
        Vision.processColor(result, Vision.greenArtifacts, Vision.purpleArtifacts)
        val second = Vision.element
        assertTrue(Vision.greenArtifacts.isNotEmpty())
        assertTrue(Vision.distance(Pose(), Pose(3.0, 4.0)) == 5.0)

        `when`(result.colorResults).thenReturn(emptyList())
        Vision.greenArtifacts.clear()
        Vision.purpleArtifacts.clear()
        Vision.processColor(result, Vision.greenArtifacts, Vision.purpleArtifacts)
        assertEquals(second, Vision.element)
    }

    @Test
    fun configurableValuesRemainMutable() {
        Vision.CAMERA_UPSIDE_DOWN = Vision.CAMERA_UPSIDE_DOWN
        Vision.CAMERA_X_INCHES = Vision.CAMERA_X_INCHES
        Vision.CAMERA_Y_INCHES = Vision.CAMERA_Y_INCHES
        Vision.CAMERA_Z_INCHES = Vision.CAMERA_Z_INCHES
        Vision.CAMERA_PITCH_DEGREES = Vision.CAMERA_PITCH_DEGREES
        Vision.CAMERA_YAW_DEGREES = Vision.CAMERA_YAW_DEGREES
        Vision.ELEMENT_RADIUS = Vision.ELEMENT_RADIUS
        Vision.ELEVATION_SCALAR = Vision.ELEVATION_SCALAR
        Vision.BEARING_X_SCALAR = Vision.BEARING_X_SCALAR
        Vision.BEARING_Y_SCALAR = Vision.BEARING_Y_SCALAR
        Vision.POS_GOAL_LOCK = Vision.POS_GOAL_LOCK
        Vision.POS_CHASE_LOCK = Vision.POS_CHASE_LOCK
        Vision.POS_MIN = Vision.POS_MIN
        Vision.POS_MAX = Vision.POS_MAX
        Vision.POS = Vision.POS
        Vision.SETTLE_SECONDS = Vision.SETTLE_SECONDS
        Vision.timer = Vision.timer
        Vision.result = Vision.result
    }
}
