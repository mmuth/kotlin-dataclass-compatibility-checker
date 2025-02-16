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
    val horsePower: Int,
    val color: Color,
    val electicVehicle: Boolean
)

enum class Color {
    LE_MANS_BLUE, GT_SILVER, ZERMATT_SILVER, ZYKLAM_RED_PEARLEFFECT
}
