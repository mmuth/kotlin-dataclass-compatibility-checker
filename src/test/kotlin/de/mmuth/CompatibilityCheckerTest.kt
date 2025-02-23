package de.mmuth

import com.github.ajalt.clikt.core.CliktError
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldContain
import java.io.File

class CompatibilityCheckerTest : DescribeSpec({

    afterEach {
        File(INPUT_FILE_BUILD_DIR).deleteRecursively()
        File(AGAINST_INPUT_FILE_BUILD_DIR).deleteRecursively()
    }

    describe("Validation will succeed for compatible changes") {
        it("is OK to add new fields") {
            val violations = validate("SimpleDataClass-Valid-AddNewFields", "SimpleDataClass-Baseline")
            violations shouldHaveSize 0
        }

        it("is OK to change nullable fields to non-null (narrowing)") {
            val violations = validate("ClassWithLists-Valid-ChangeNullableTypeToNonNullable", "ClassWithLists-Baseline")
            violations shouldHaveSize 0
        }

        it("is OK to add a value to an enum") {
            val violations = validate("SimpleDataClassWithEnum-Valid-AddedValueToEnum", "SimpleDataClassWithEnum-Baseline")
            violations shouldHaveSize 0
        }

        it("will also work for nested types (referenced data classes and enums)") {
            val fieldViolations = validate("DataClassWithNestedMembers-Valid-AddedFieldToSubType", "DataClassWithNestedMembers-Baseline")
            val enumViolations = validate("DataClassWithNestedMembers-Valid-AddedValueToSubEnum", "DataClassWithNestedMembers-Baseline")
            fieldViolations shouldHaveSize 0
            enumViolations shouldHaveSize 0
        }

        it("will also work for deeper nested types") {
            val violations = validate("ComplexDataClass-Valid-AddFieldInSubTypeAndEnum", "ComplexDataClass-Baseline")
            violations shouldHaveSize 0
        }
    }

    describe("Validation will fail for incompatible changes") {
        it("is BREAKING if a member is removed") {
            val violations = validate("SimpleDataClass-Invalid-RemoveField", "SimpleDataClass-Baseline")
            violations.shouldContainExactly("Type 'SimpleDataClass', Member 'count' was removed")
        }

        it("is BREAKING if a non-nullable type should be made nullable (opened)") {
            val violations = validate("ClassWithLists-Invalid-NullForNonNullableField", "ClassWithLists-Baseline")
            violations.shouldContainExactly("Type 'ClassWithLists', Member 'count': types are not compatible: kotlin.Int? vs. kotlin.Int")
        }

        it("is BREAKING if the type of a member is changed") {
            val violations = validate("SimpleDataClass-Invalid-MemberTypeDoesNotMatch", "SimpleDataClass-Baseline")
            violations.shouldContainExactly("Type 'SimpleDataClass', Member 'count': types are not compatible: kotlin.Long vs. kotlin.Int")
        }

        it("is BREAKING if the value of an enum is removed") {
            val violations = validate("SimpleDataClassWithEnum-Invalid-RemovedValueFromEnum", "SimpleDataClassWithEnum-Baseline")
            violations.shouldContainExactly(
                "Enum 'Color': value 'GREEN' was removed",
                "Enum 'Color': value 'BLUE' was removed",
                "Enum 'Color': value 'PURPLE' was removed"
            )
        }

        it("will detect breaking changes also on nested types (referenced data classes and enums)") {
            val violations = validate("DataClassWithNestedMembers-Invalid-RemovedValuesFromSubTypes", "DataClassWithNestedMembers-Baseline")
            violations.shouldContainExactly(
                "Type 'Car', Member 'horsePower' was removed",
                "Enum 'Color': value 'ZERMATT_SILVER' was removed",
                "Enum 'Color': value 'ZYKLAM_RED_PEARLEFFECT' was removed"
            )
        }

        it("will detect breaking changes also for deeper nested types") {
            val violations = validate("ComplexDataClass-Invalid-ChangeFieldToNullableInSubtype", "ComplexDataClass-Baseline")
            violations.shouldContainExactly("Type 'Address', Member 'city': types are not compatible: kotlin.String? vs. kotlin.String")
        }
    }

    describe("Validation will fail for unsupported type constellations or rough mismatches") {
        it("will fail if classes are not data classes or enums") {
            val exception = shouldThrow<CliktError> {
                validate("Invalid-NotADataClass", "SimpleDataClass-Baseline")
            }

            exception.message shouldContain "only data classes and enums are supported"
        }

        it("will fail if the main classes are not even matching") {
            val inputClass = KotlinValidatableDataClassDescription("Boats", "com.testdata.anotherpackage", emptyList(), emptySet())
            val totallyDifferentOtherClass = KotlinValidatableDataClassDescription("Cars", "com.testdata.anotherpackage", emptyList(), emptySet())
            val violations = Validator().check(inputClass, totallyDifferentOtherClass)
            violations.shouldContainExactly("Main data class names do not match: 'Boats' vs. 'Cars'. Stopping.")
        }

        it("will fail for multiple nullable types within one type reference") {
            val inputViolations = validate("Invalid-UnsupportedNullabilities", "SimpleDataClass-Baseline")
            val expectation = "Sorry, inputs contain unsupported typings - currently only one nullable type per type reference is supported. Stopping."
            inputViolations.shouldContainExactly(expectation)
        }
    }

})

private fun validate(inputFile: String, againstInputFile: String): Set<Violation> {
    val mainClassNameFromFileName = inputFile.split("-")[0]
    val inputFilePath = "src/test/resources/testclasses/$inputFile.kt"
    val againstInputFilePath = "src/test/resources/testclasses/$againstInputFile.kt"
    val loadedClasses = ExternalClassLoader(inputFilePath, againstInputFilePath, mainClassNameFromFileName).load()
    val violations = Validator().check(loadedClasses.first, loadedClasses.second)
    println("Validation Results: $violations")
    return violations
}


