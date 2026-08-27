package org.firstinspires.ftc.threedrd.nextftc.telemetry

import com.bylazar.configurables.annotations.Configurable
import com.bylazar.configurables.annotations.IgnoreConfigurable
import com.bylazar.telemetry.JoinedTelemetry
import com.bylazar.telemetry.PanelsTelemetry
import dev.nextftc.ftc.ActiveOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry as FtcTelemetry
import org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.HTML
import org.firstinspires.ftc.threedrd.nextftc.config.Diagnostics
import org.firstinspires.ftc.threedrd.nextftc.config.toTelLevel
import org.firstinspires.ftc.threedrd.nextftc.logging.Logging
import org.firstinspires.ftc.threedrd.nextftc.telemetry.TelemetryLevel.OFF

@Configurable
object Telemetry {
    var LEVEL = OFF
    var FILTER = ""
    @field:IgnoreConfigurable
    var DISPLAY_FILTER = ""

    @field:IgnoreConfigurable
    lateinit var output: FtcTelemetry

    @field:IgnoreConfigurable
    val sections = mutableSetOf<String>()

    @field:IgnoreConfigurable
    private var diagnostics: Diagnostics? = null
    @field:IgnoreConfigurable
    private var configuredLevel = OFF

    fun initialize() {
        diagnostics = null
        bind(null)
        ActiveOpMode.telemetry.setDisplayFormat(HTML)
        output = JoinedTelemetry(ActiveOpMode.telemetry, PanelsTelemetry.ftcTelemetry)
        sections.clear()
        Logging.initialize()
    }

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

    fun bind(config: Diagnostics?) {
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

    fun Diagnostics?.level() = if (this == null) OFF else level().toTelLevel()
    fun Diagnostics?.filter() = if (this == null) "" else filter()

    fun config(caption: String, value: Any?) {
        section("CONFIG")
        output.addData(caption, value)
    }

    fun configWarning(message: String) {
        val warning = "WARNING: $message"
        ActiveOpMode.telemetry.addLine("<b><font color='#FFC107'>$warning</font></b>")
        ActiveOpMode.telemetry.addLine()
        PanelsTelemetry.ftcTelemetry.addLine(warning)
        PanelsTelemetry.ftcTelemetry.addLine()
    }

    fun add(source: String, level: TelemetryLevel, caption: String, value: Any?): Boolean {
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
    fun add(level: TelemetryLevel, caption: String, value: Any?) = Telemetry.add(source, level, caption, value)
    fun verbose(caption: String, value: Any?) = add(TelemetryLevel.VERBOSE, caption, value)
    fun debug(caption: String, value: Any?) = add(TelemetryLevel.DEBUG, caption, value)
    fun info(caption: String, value: Any?) = add(TelemetryLevel.INFO, caption, value)
    fun warn(caption: String, value: Any?) = add(TelemetryLevel.WARN, caption, value)
    fun error(caption: String, value: Any?) = add(TelemetryLevel.ERROR, caption, value)
    fun fatal(caption: String, value: Any?) = add(TelemetryLevel.ASSERT, caption, value)

    fun add(level: TelemetryLevel, caption: String, value: () -> Any?) {
        if (Telemetry.LEVEL.accepts(level)) add(level, caption, value())
    }

    fun verbose(caption: String, value: () -> Any?) = add(TelemetryLevel.VERBOSE, caption, value)
    fun debug(caption: String, value: () -> Any?) = add(TelemetryLevel.DEBUG, caption, value)
    fun info(caption: String, value: () -> Any?) = add(TelemetryLevel.INFO, caption, value)
    fun warn(caption: String, value: () -> Any?) = add(TelemetryLevel.WARN, caption, value)
    fun error(caption: String, value: () -> Any?) = add(TelemetryLevel.ERROR, caption, value)
    fun fatal(caption: String, value: () -> Any?) = add(TelemetryLevel.ASSERT, caption, value)
}
