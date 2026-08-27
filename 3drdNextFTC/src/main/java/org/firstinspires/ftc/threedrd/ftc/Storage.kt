package org.firstinspires.ftc.threedrd.ftc

import java.io.File

open class Storage(val file: File) {
    constructor(fileName: String) : this(File("/sdcard/FIRST/settings", fileName))

    open fun read(): String? {
        return if (file.exists()) file.readText() else null
    }

    open fun write(value: String) {
        file.absoluteFile.parentFile!!.mkdirs()
        file.writeText(value)
    }
}
