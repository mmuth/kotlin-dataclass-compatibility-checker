package com.testdata.somepackage

import java.time.Instant
import java.time.LocalDateTime

data class Car(
    val manufacturer: String,
    val year: Int,
    val model: String,
    val horsePower: Int,
    val color: Color,
    val owner: Owner,
)

data class Owner(
    val name: String,
    val dateOfBirth: Instant,
)

enum class Color {
    LE_MANS_BLUE,
    GT_SILVER,
    DRAGON_GREEN,
    TORNADO_RED
}

