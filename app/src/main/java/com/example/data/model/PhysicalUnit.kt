package com.example.data.model

import java.util.Locale

enum class UnitCategory(val displayName: String, val icon: String) {
    WEIGHT("Weight", "⚖️"),
    LENGTH("Length", "📏")
}

data class PhysicalUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    val factorToBase: Double, // Base SI: kg for WEIGHT, m for LENGTH
    val icon: String = ""
) {
    companion object {
        val WEIGHT_UNITS = listOf(
            PhysicalUnit("kg", "Kilogram", "kg", UnitCategory.WEIGHT, 1.0, "⚖️"),
            PhysicalUnit("g", "Gram", "g", UnitCategory.WEIGHT, 0.001, "🧪"),
            PhysicalUnit("mg", "Milligram", "mg", UnitCategory.WEIGHT, 0.000001, "💊"),
            PhysicalUnit("t", "Metric Ton", "t", UnitCategory.WEIGHT, 1000.0, "🚛"),
            PhysicalUnit("lb", "Pound", "lb", UnitCategory.WEIGHT, 0.45359237, "🏋️"),
            PhysicalUnit("oz", "Ounce", "oz", UnitCategory.WEIGHT, 0.028349523125, "🪙"),
            PhysicalUnit("st", "Stone", "st", UnitCategory.WEIGHT, 6.35029318, "🪨"),
            PhysicalUnit("ct", "Carat", "ct", UnitCategory.WEIGHT, 0.0002, "💎")
        )

        val LENGTH_UNITS = listOf(
            PhysicalUnit("m", "Meter", "m", UnitCategory.LENGTH, 1.0, "📏"),
            PhysicalUnit("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0, "🛣️"),
            PhysicalUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01, "📐"),
            PhysicalUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001, "🔬"),
            PhysicalUnit("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344, "🚗"),
            PhysicalUnit("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144, "⛳"),
            PhysicalUnit("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048, "🦶"),
            PhysicalUnit("in", "Inch", "in", UnitCategory.LENGTH, 0.0254, "📱"),
            PhysicalUnit("NM", "Nautical Mile", "NM", UnitCategory.LENGTH, 1852.0, "⚓")
        )

        val ALL_UNITS = WEIGHT_UNITS + LENGTH_UNITS

        val DEFAULT_WEIGHT_FROM = WEIGHT_UNITS.first { it.id == "kg" }
        val DEFAULT_WEIGHT_TO = WEIGHT_UNITS.first { it.id == "lb" }

        val DEFAULT_LENGTH_FROM = LENGTH_UNITS.first { it.id == "m" }
        val DEFAULT_LENGTH_TO = LENGTH_UNITS.first { it.id == "ft" }

        fun getUnitsForCategory(category: UnitCategory): List<PhysicalUnit> = when (category) {
            UnitCategory.WEIGHT -> WEIGHT_UNITS
            UnitCategory.LENGTH -> LENGTH_UNITS
        }

        fun find(id: String): PhysicalUnit {
            return ALL_UNITS.find { it.id.equals(id, ignoreCase = true) }
                ?: PhysicalUnit(id, id, id, UnitCategory.WEIGHT, 1.0, "📐")
        }

        fun convert(value: Double, from: PhysicalUnit, to: PhysicalUnit): Double {
            if (from.id.equals(to.id, ignoreCase = true)) return value
            val inBase = value * from.factorToBase
            return inBase / to.factorToBase
        }

        fun formatUnitValue(value: Double): String {
            if (value == 0.0) return "0"
            val absVal = kotlin.math.abs(value)
            return when {
                value == value.toLong().toDouble() -> value.toLong().toString()
                absVal < 0.000001 -> String.format(Locale.US, "%.4e", value)
                absVal < 0.001 -> String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
                absVal < 1.0 -> String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
                else -> String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
            }
        }
    }
}
