package org.firstinspires.ftc.teamcode.adaptations.nextftc.config

import java.lang.reflect.Field
import kotlin.reflect.full.primaryConstructor

fun Any.settings() = settings(javaClass.declaredFields.asIterable())

internal fun Any.settings(fields: Iterable<Field>): List<SettingItem> {
    val remaining = fields
        .mapNotNull { field ->
            field.getAnnotation(Setting::class.java)?.let { field.name to (field to it) }
        }.toMap(linkedMapOf())
    val ordered = this::class.primaryConstructor?.parameters.orEmpty()
        .mapNotNull { parameter -> remaining.remove(parameter.name!!) }

    return (ordered + remaining.values).map { (field, setting) ->
        field.isAccessible = true
        field.toSettingItem(this, setting)
    }
}

fun Field.toSettingItem(owner: Any, setting: Setting): SettingItem {
    val key = setting.name.ifEmpty { name.humanize() }

    return when {
        type.isEnum -> enumSetting(owner, key, setting)
        type == Boolean::class.javaPrimitiveType -> booleanSetting(owner, key, setting)
        type == Double::class.javaPrimitiveType -> doubleSetting(owner, key, setting)
        type == String::class.java -> stringSetting(owner, key, setting)
        else -> error("Unsupported @Setting type for $name: ${type.simpleName}")
    }
}

fun Field.stringSetting(owner: Any, key: String, setting: Setting): SettingItem {
    val provider = setting.options.objectInstance
        ?: setting.options.java.getDeclaredConstructor().newInstance()

    return SettingItem(
        key,
        { get(owner) ?: "None" },
        { direction ->
            val values = provider.options()
            if (values.isEmpty()) return@SettingItem false
            val before = get(owner) as String?
            val index = values.indexOf(before)
            val after = values[if (index < 0) {
                if (direction > 0) 0 else values.lastIndex
            } else Math.floorMod(index + direction, values.size)]
            set(owner, after)
            before != after
        },
        setting.live
    )
}

fun Field.enumSetting(owner: Any, key: String, setting: Setting): SettingItem {
    val values = type.enumConstants.orEmpty().filterNot { (it as Enum<*>).name == "UNKNOWN" }
    require(values.isNotEmpty()) { "@Setting enum $name has no selectable values" }

    return SettingItem(
        key,
        { get(owner) },
        { direction ->
            val before = get(owner)
            val index = values.indexOf(before)
            set(owner, values[if (index < 0) 0 else Math.floorMod(index + direction, values.size)])
            before != get(owner)
        },
        setting.live
    )
}

fun Field.booleanSetting(owner: Any, key: String, setting: Setting) = SettingItem(
    key,
    { getBoolean(owner) },
    {
        setBoolean(owner, !getBoolean(owner))
        true
    },
    setting.live
)

fun Field.doubleSetting(owner: Any, key: String, setting: Setting): SettingItem {
    require(!setting.inc.isNaN() && setting.inc > 0.0) {
        "@Setting double $name requires a positive increment"
    }
    require(setting.min <= setting.max) {
        "@Setting double $name has an invalid range"
    }

    return SettingItem(
        key,
        {
            val value = getDouble(owner)
            if (setting.format.isEmpty()) value else setting.format.format(value)
        },
        { direction ->
            val before = getDouble(owner)
            val after = (before + setting.inc * direction).coerceIn(setting.min, setting.max)
            setDouble(owner, after)
            before != after
        },
        setting.live
    )
}

fun String.humanize() =
    replace(Regex("([a-z])([A-Z])"), "$1 $2").replaceFirstChar { it.uppercase() }
