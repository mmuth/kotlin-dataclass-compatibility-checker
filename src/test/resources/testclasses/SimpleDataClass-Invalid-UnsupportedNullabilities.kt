package com.testdata.anotherpackage

data class SimpleDataClass(
    // we currently just do not support more than one nullable flag in a type
    // it seems not feasible in the use case we are targeting and is harder to validate
    // however we should detect it and stop further validation
    val currentlyNotSupported: Map<String?, String?>?
)
