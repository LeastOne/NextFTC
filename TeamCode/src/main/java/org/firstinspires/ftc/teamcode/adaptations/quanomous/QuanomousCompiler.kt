package org.firstinspires.ftc.teamcode.adaptations.quanomous

import com.google.gson.JsonObject
import dev.nextftc.core.commands.Command
import dev.nextftc.core.commands.groups.SequentialGroup

class QuanomousCompiler(
    val commands: Map<String, (JsonObject) -> Command>
) {
    fun load(name: String) = compile(Quanomous.load(name))

    fun compile(steps: Iterable<com.google.gson.JsonElement>): Command {
        val created = steps.map { element ->
            val step = element.asJsonObject
            val name = step["cmd"].asString
            require(commands.containsKey(name)) { "Unknown Quanomous command: $name" }
            commands.getValue(name)(step)
        }
        return SequentialGroup(*created.toTypedArray())
    }
}
