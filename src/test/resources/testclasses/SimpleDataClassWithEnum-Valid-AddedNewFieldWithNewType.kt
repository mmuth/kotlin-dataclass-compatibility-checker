package com.testdata.anotherpackage

data class SimpleDataClassWithEnum(
    val name: String,
    val count: Int,
    val color: Color,
    val newFieldWithNewType: AnotherSample
)

enum class Color {
    RED, GREEN, BLUE, PURPLE
}

data class AnotherSample(
    val random: Int
)
