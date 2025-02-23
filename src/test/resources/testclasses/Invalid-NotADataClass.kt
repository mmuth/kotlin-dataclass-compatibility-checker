package com.testdata.anotherpackage

class Car(
    val make: String,
    val model: String,
    val year: Int,
    var mileage: Int
) {

    fun drive(distance: Int) {
        if (distance > 0) {
            mileage += distance
            println("You drove $distance miles. Total mileage is now $mileage miles.")
        } else {
            println("Distance must be positive.")
        }
    }

    fun displayInfo() {
        println("Car Info: $year $make $model with $mileage miles.")
    }
}

class Invalid {
    val onlyDataClassesAreSupported: Car? = null
}

fun main() {
    val myCar = Car("Audi", "Coupe GT", 1987, 338950)
    myCar.displayInfo()
    myCar.drive(120)
    myCar.displayInfo()
}




