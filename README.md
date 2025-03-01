# kotlin-dataclass-compatibility-checker

This is a small cli to validate the compatibility of two kotlin data classes against each other.
It is useful for DTOs that are shared in two (or more) locations and you want to ensure that you don't change it
in an incompatible way (e.g. DTO shared between Client and Server or between two or more microservices).

You can compare it to the breaking change detection in [Protobufs](https://buf.build/docs/breaking/overview/) `buf` util.

## Usage

```
java -jar kotlin-dataclass-compatibility-checker.jar \ 
  --input ./Updated/MyClass.kt \
  --against ./Baseline/MyClass.kt
```

## Validation

Typical breaking changes are detected:

- field types do not match
- fields were removed
- enum values were removed
- sealed class members were removed

e.g. validating
<table>
  <tr>
    <th>input</th>
    <th>against ("current schema")</th>
  </tr>
  <tr>
    <td>

    data class Car(
      val manufacturer: String,
      //              <== dropped model
      val year: Int?, // <== now nullable
      val color: Color,
      val owner: Owner
    )

    data class Owner(
      //              <== dropped name
      val dateOfBirth: Instant,
      val favoriteFood: String // new (OK)
    )
    
    enum class Color {
      LE_MANS_BLUE,
      GT_SILVER,
      // <== removed one
      TORNADO_RED
    }

</td>
<td>

    data class Car(
      val manufacturer: String,
      val model: String
      val year: Int,
      val color: Color,
      val owner: Owner
    )
    
    data class Owner(
      val name: String,
      val dateOfBirth: Instant           
    )
    
    enum class Color {
      LE_MANS_BLUE,
      GT_SILVER,
      DRAGON_GREEN,
      TORNADO_RED
    }

</td></tr></table> 

will result in:

```
[ERROR] Type 'Car', Member 'model' was removed
[ERROR] Type 'Car', Member 'year': types are not compatible: kotlin.Int? vs. kotlin.Int
[ERROR] Enum 'Color': value 'DRAGON_GREEN' was removed
[ERROR] Type 'Owner', Member 'name' was removed
```

This small sample and also an equivalent in protobuf representation is located in the sample directory.
You can alternatively check the tests in `src/test/resources` for more examples.

## Constraints

* Currently, exactly one kotlin main data class is supported for the validation. However, using other classes (referenced from the main class) in the
  to-be-validated class is supported, as long as everything is in one file :-).
* there is no support for nested classes yet. If you plan to use sealed classes to model something like `oneOf` just define it in an unnested style.

You may also take a look at the [tests](./src/test/resources) to get an impression of the current use cases.

## Contributing

Feel free to contribute.

## Other

Dedicated to [meisterplan.com](https://meisterplan.com), the greatest PPM Tool in the world. \
A big shoutout to the awesome people working there ❤️

