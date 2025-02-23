package com.testdata.somepackage

import java.time.Instant
import java.time.LocalDateTime

data class Car(
    val manufacturer: String,
    val year: Int?,
    val horsePower: Int,
    val color: Color,
    val owner: Owner,
)

data class Owner(
    val dateOfBirth: Instant,
    val favoriteFood: String
)

enum class Color {
    LE_MANS_BLUE,
    GT_SILVER,
    TORNADO_RED
}
