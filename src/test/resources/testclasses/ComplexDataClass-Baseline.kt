package com.testdata.somepackage

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime

data class ComplexDataClass(
    val name: String,
    val age: Int,
    val garagesByHouse: Map<House, Garage>,
    val carsByColor: Map<Color, List<Car>>,
    val vehicles: List<Vehicle>,
) {
    // sample method that should just be ignored by the validation
    fun isCarInGarage(garage: Garage, car: Car): Boolean {
        return garage.cars.contains(car)
    }
}

data class House(
    val name: String,
    val address: Address,
    val garage: Garage?
)

data class Garage(
    val name: String,
    val cars: List<Car>
)

data class Address(
    val street: String,
    val city: String,
    val zipCode: String
)

sealed class Vehicle

data class Car(
    val name: String,
    val year: Int,
    val model: String,
    val horsePower: Int,
    val consumption: BigDecimal,
    val bought: LocalDateTime,
    val lastService: Instant
) : Vehicle()

data class MotorCycle(
    val name: String,
    val year: Int,
    val model: String,
    val horsePower: Int,
) : Vehicle()

data class Truck(
    val name: String,
    val year: Int,
    val model: String,
) : Vehicle()

enum class Color {
    LE_MANS_BLUE,
    GT_SILVER,
    ZERMATT_SILVER,
    ZYKLAM_RED_PEARLEFFECT,
    DRAGON_GREEN
}

