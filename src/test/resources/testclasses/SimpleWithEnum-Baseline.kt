package com.testdata.somepackage

data class SimpleWithEnum(
    val name: String,
    val count: Int,
    val color: Color
)

enum class Color {
    RED, GREEN, BLUE, PURPLE
}
