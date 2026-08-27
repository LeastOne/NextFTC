package org.firstinspires.ftc.threedrd.nextftc.logging

data class LogEntry(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val context: String = ""
) {
    val line = "${level.indicator} | $tag | $message"
    val searchableText = listOf(level, tag, message, context).joinToString(" ")

    fun matches(filter: String) = searchableText.contains(filter, ignoreCase = true)
}
