# kotlin-dataclass-compatibility-checker

This is a small cli to validate the compatibility of two kotlin data classes against each other.
It is useful for DTOs that are shared in two (or more) locations and you want to ensure that you don't change it
in an incompatible way (e.g. DTO shared between Client and Server or between two or more microservices).

You can compare it to the breaking change detection in [Protobufs](https://buf.build/docs/breaking/overview/) `buf` util.

## Usage

```
kotlin-dataclass-compatibility-checker --input ./Updated/MyClass.kt --against-input ./Baseline/MyClass.kt
```

## Constraints

Currently, exactly one kotlin main data class is supported for the validation. However, using other classes (nested) in the to-be-validated
class is supported, as long as everything is in one file :-).

## Other

Feel free to contribute.
