package org.firstinspires.ftc.threedrd.nextftc.subsystems

import dev.nextftc.ftc.ActiveOpMode
import dev.nextftc.ftc.Gamepads.gamepad1
import dev.nextftc.ftc.Gamepads.gamepad2
import org.firstinspires.ftc.threedrd.nextftc.config.ConfigComponent
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics
import org.firstinspires.ftc.threedrd.nextftc.config.SettingItem
import org.firstinspires.ftc.threedrd.nextftc.config.settings
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.opmodes.isAutonomous
import org.firstinspires.ftc.threedrd.nextftc.opmodes.isTeleop
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem.Change.NEXT
import org.firstinspires.ftc.threedrd.nextftc.subsystems.ConfigSubsystem.Change.PREV
import org.firstinspires.ftc.threedrd.nextftc.telemetry.Telemetry

abstract class ConfigSubsystem : Subsystem() {
    abstract val config: Any

    class State(
        var auto: Boolean = false,
        var teleop: Boolean = false,
        var started: Boolean = false,
        var interrupt: Boolean = false,
        var configurable: Boolean = false,
        var setting: Int = 0,
    )

    val state = State()
    val items by lazy { config.settings() }

    val edit by instant { state.configurable = true }
    val done by instant { state.configurable = false }
    val prevItem by instant { changeItem(PREV) }
    val nextItem by instant { changeItem(NEXT) }
    val prevValue by instant { changeValue(PREV) }
    val nextValue by instant { changeValue(NEXT) }

    override fun initialize() {
        val diagnostics = Diagnostics(config)
        Telemetry.bind(diagnostics)
        Logging.bind(diagnostics)

        state.auto = ActiveOpMode.isAutonomous
        state.teleop = ActiveOpMode.isTeleop
        state.interrupt = state.teleop
        state.started = false
        state.setting = 0
        state.configurable = false

        val editing = gamepad1.back or gamepad2.back
        editing whenBecomesTrue edit
        editing whenBecomesFalse done
        (gamepad1.dpadUp or gamepad2.dpadUp) whenBecomesTrue prevItem
        (gamepad1.dpadDown or gamepad2.dpadDown) whenBecomesTrue nextItem
        (gamepad1.dpadLeft or gamepad2.dpadLeft) whenBecomesTrue prevValue
        (gamepad1.dpadRight or gamepad2.dpadRight) whenBecomesTrue nextValue
    }

    override fun periodic() {
        items.forEach { Telemetry.config(caption(it), it.value()) }
    }

    override fun start() {
        state.started = true
    }

    fun changeItem(change: Change) {
        if (state.configurable)
            state.setting = (state.setting + change.sign).coerceIn(0, items.lastIndex)
    }

    fun changeValue(change: Change) {
        val item = items[state.setting]
        if (state.configurable && (!state.started || item.live) && item.change(change.sign)) {
            ConfigComponent.changed()
            onChange(item)
        }
    }

    open fun onChange(item: SettingItem) = Unit

    fun caption(item: SettingItem): String {
        if (!state.configurable || items[state.setting] != item) return item.key
        return "${if (!state.started || item.live) ">" else "x"}${item.key}"
    }

    enum class Change(val sign: Int) {
        PREV(-1), NEXT(1)
    }
}
