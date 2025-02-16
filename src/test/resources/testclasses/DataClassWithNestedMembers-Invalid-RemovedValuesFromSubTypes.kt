package com.testdata.anotherpackage

data class DataClassWithNestedMembers(
    val name: String,
    val age: Int,
    val garage: List<Car>
)

data class Car(
    val name: String,
    val year: Int,
    val model: String,
    val color: Color
)

enum class Color {
    LE_MANS_BLUE, GT_SILVER
}
