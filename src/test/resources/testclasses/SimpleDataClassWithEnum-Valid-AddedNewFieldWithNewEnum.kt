package com.testdata.anotherpackage

data class SimpleDataClassWithEnum(
    val name: String,
    val count: Int,
    val color: Color,
    val newFieldWithNewEnum: Brightness
)

enum class Color {
    RED, GREEN, BLUE, PURPLE
}

enum class Brightness {
    LOW, MEDIUM, HIGH
}
