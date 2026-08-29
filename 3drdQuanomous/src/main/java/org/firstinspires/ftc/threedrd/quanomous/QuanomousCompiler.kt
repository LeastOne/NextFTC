package org.firstinspires.ftc.threedrd.quanomous

import com.google.gson.JsonObject

class QuanomousCompiler<T>(
    val commands: Map<String, (JsonObject) -> T>
) {
    fun load(name: String) = compile(Quanomous.load(name))

    fun compile(steps: Iterable<com.google.gson.JsonElement>) =
        steps.map { element ->
            val step = element.asJsonObject
            val name = step["cmd"].asString
            require(commands.containsKey(name)) { "Unknown Quanomous command: $name" }
            commands.getValue(name)(step)
        }
}
