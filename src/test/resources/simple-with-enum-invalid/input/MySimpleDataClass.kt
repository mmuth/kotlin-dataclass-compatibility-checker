package com.testdata.somepackage

data class MySimpleDataClass(
    val name: String,
    val count: Int,
    val color: Color
)

enum class Color {
    RED, GREEN, BLUE, PURPLE, CYAN
}
