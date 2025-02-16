package com.testdata.anotherpackage

data class SimpleDataClassWithEnum(
    val name: String,
    val count: Int,
    val color: Color
)

enum class Color {
    RED, GREEN, BLUE, PURPLE, CYAN
}
