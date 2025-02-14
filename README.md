# kotlin-dataclass-compatibility-checker

This is a small cli to validate the compatibility of two kotlin data classes against each other.
It is useful for DTOs and comparable to the breaking change detection in [Protobufs](https://buf.build/docs/breaking/overview/) `buf` util.

## Usage

```
kotlin-dataclass-compatibility-checker --input ./MyDataClass.kt --against-input ./MyDataClassChanged.kt
```

## Constraints

Currently, exactly one kotlin main data class is supported for the validation. However, using other classes (nested) in the to-be-validated
class is supported, as long as everything is in one file :-).

## Other

Feel free to contribute.