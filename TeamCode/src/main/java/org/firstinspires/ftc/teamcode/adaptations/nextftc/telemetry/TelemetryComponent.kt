package org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry

import com.bylazar.configurables.annotations.Configurable
import com.bylazar.configurables.annotations.IgnoreConfigurable
import com.bylazar.telemetry.JoinedTelemetry
import com.bylazar.telemetry.PanelsTelemetry
import dev.nextftc.core.components.Component
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry as FtcTelemetry
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.DiagnosticsConfig
import org.firstinspires.ftc.teamcode.adaptations.nextftc.config.toTelLevel
import org.firstinspires.ftc.teamcode.adaptations.nextftc.logging.Logging
import org.firstinspires.ftc.teamcode.adaptations.nextftc.telemetry.Level.OFF

@Configurable
object TelemetryComponent : Component {
    var LEVEL = OFF
    var FILTER = ""
    @field:IgnoreConfigurable
    var DISPLAY_FILTER = ""

    @field:IgnoreConfigurable
    lateinit var output: FtcTelemetry

    @field:IgnoreConfigurable
    val sections = mutableSetOf<String>()

    @field:IgnoreConfigurable
    private var diagnostics: DiagnosticsConfig? = null
    @field:IgnoreConfigurable
    private var configuredLevel = OFF

    override fun preInit() {
        diagnostics = null
        bind(null)
        output = JoinedTelemetry(ActiveOpMode.telemetry, PanelsTelemetry.ftcTelemetry)
        sections.clear()
        Logging.initialize()
    }

    override fun preWaitForStart() = beginFrame()
    override fun preUpdate() = beginFrame()
    override fun postWaitForStart() = update()
    override fun postUpdate() = update()

    fun beginFrame() {
        sampleDiagnostics()
        output.clear()
        sections.clear()
        Logging.beginFrame()
    }

    fun update() {
        if (Logging.visibleEntries().isNotEmpty()) section("LOG")
        output.update()
    }

    fun bind(config: DiagnosticsConfig?) {
        diagnostics = config
        configuredLevel = config.level()
        LEVEL = configuredLevel
        DISPLAY_FILTER = config.filter()
    }

    fun sampleDiagnostics() {
        val config = diagnostics
        val nextLevel = config.level()
        if (LEVEL == configuredLevel) LEVEL = nextLevel
        configuredLevel = nextLevel
        DISPLAY_FILTER = config.filter()
    }

    fun DiagnosticsConfig?.level() = if (this == null) OFF else level.toTelLevel()
    fun DiagnosticsConfig?.filter() = if (this == null) "" else filter

    fun config(caption: String, value: Any?) {
        section("CONFIG")
        output.addData(caption, value)
    }

    fun add(source: String, level: Level, caption: String, value: Any?): Boolean {
        val text = listOf(level, source, caption, value).joinToString(" ")
        if (!LEVEL.accepts(level) || !text.matches(DISPLAY_FILTER) || !text.matches(FILTER)) return false

        section("TEL")
        output.addData("${level.indicator} | $source | $caption", value)
        return true
    }

    fun section(title: String) {
        if (sections.add(title)) output.addLine(title(title))
    }

    fun title(title: String): String {
        val remaining = 61 - title.length - 2
        val left = remaining / 2
        return "-".repeat(left) + " $title " + "-".repeat(remaining - left)
    }

    fun String.matches(filter: String) = contains(filter, ignoreCase = true)
}

class Tel(val source: String) {
    fun add(level: Level, caption: String, value: Any?) = TelemetryComponent.add(source, level, caption, value)
    fun verbose(caption: String, value: Any?) = add(Level.VERBOSE, caption, value)
    fun debug(caption: String, value: Any?) = add(Level.DEBUG, caption, value)
    fun info(caption: String, value: Any?) = add(Level.INFO, caption, value)
    fun warn(caption: String, value: Any?) = add(Level.WARN, caption, value)
    fun error(caption: String, value: Any?) = add(Level.ERROR, caption, value)
    fun fatal(caption: String, value: Any?) = add(Level.ASSERT, caption, value)

    fun add(level: Level, caption: String, value: () -> Any?) {
        if (TelemetryComponent.LEVEL.accepts(level)) add(level, caption, value())
    }

    fun verbose(caption: String, value: () -> Any?) = add(Level.VERBOSE, caption, value)
    fun debug(caption: String, value: () -> Any?) = add(Level.DEBUG, caption, value)
    fun info(caption: String, value: () -> Any?) = add(Level.INFO, caption, value)
    fun warn(caption: String, value: () -> Any?) = add(Level.WARN, caption, value)
    fun error(caption: String, value: () -> Any?) = add(Level.ERROR, caption, value)
    fun fatal(caption: String, value: () -> Any?) = add(Level.ASSERT, caption, value)
}
