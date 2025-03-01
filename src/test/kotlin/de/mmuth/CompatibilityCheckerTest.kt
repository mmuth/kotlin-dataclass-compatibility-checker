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

        it("is OK to add a value to an enum") {
            val violations = validate("SimpleDataClassWithEnum-Valid-AddedValueToEnum", "SimpleDataClassWithEnum-Baseline")
            violations shouldHaveSize 0
        }

        it("is OK to add a new data class and reference it in a new field") {
            val violations = validate("SimpleDataClassWithEnum-Valid-AddedNewFieldWithNewType", "SimpleDataClassWithEnum-Baseline")
            violations shouldHaveSize 0
        }

        it("is OK to add a new enum and reference it in a new field") {
            val violations = validate("SimpleDataClassWithEnum-Valid-AddedNewFieldWithNewEnum", "SimpleDataClassWithEnum-Baseline")
            violations shouldHaveSize 0
        }

        it("will also work for nested types (referenced data classes and enums)") {
            val fieldViolations = validate("DataClassWithNestedMembers-Valid-AddedFieldToSubType", "DataClassWithNestedMembers-Baseline")
            val enumViolations = validate("DataClassWithNestedMembers-Valid-AddedValueToSubEnum", "DataClassWithNestedMembers-Baseline")
            fieldViolations shouldHaveSize 0
            enumViolations shouldHaveSize 0
        }

        it("will also work for deeper nested types") {
            val violations = validate("ComplexDataClass-Valid-AddFieldInSubTypeAndEnumAndSealedClass", "ComplexDataClass-Baseline")
            violations shouldHaveSize 0
        }

        it("will ignore unrelated changes that do not affect the main data class") {
            val violations = validate("SimpleDataClassWithEnum-Valid-AddedUnrelatedClass", "SimpleDataClassWithEnum-Baseline")
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
            val violations = validate("ComplexDataClass-Invalid-ChangeFieldTypeInReferencedType", "ComplexDataClass-Baseline")
            violations.shouldContainExactly(
                "Type 'Address', Member 'zipCode': types are not compatible: kotlin.Int vs. kotlin.String"
            )
        }

        it("will detect breaking changes also for sealed classes") {
            val violations = validate("ComplexDataClass-Invalid-SubtypeOfSealedClassRemoved", "ComplexDataClass-Baseline")
            violations.shouldContainExactly(
                "Enum 'Color': value 'ZYKLAM_RED_PEARLEFFECT' was removed",
                "Sealed class 'Vehicle': subclass 'Truck' was removed",
                "Type 'MotorCycle', Member 'horsePower' was removed",
                "Referenced type 'Truck' does not exist in input class"
            )
        }
    }

    describe("Validation will fail for unsupported type constellations or rough mismatches") {
        it("will fail if classes are not data classes or enums") {
            val exception = shouldThrow<CliktError> {
                validate("Invalid-NotADataClass", "SimpleDataClass-Baseline")
            }

            exception.message shouldContain "only data classes, sealed classes and enums are supported"
        }

        it("will fail if the main classes are not even matching") {
            val inputClass = KotlinValidatableClassDescription("Boats", "com.testdata.anotherpackage", emptyList(), emptySet(), emptySet())
            val totallyDifferentOtherClass = KotlinValidatableClassDescription("Cars", "com.testdata.anotherpackage", emptyList(), emptySet(), emptySet())
            val violations = Validator().check(inputClass, totallyDifferentOtherClass)
            violations.shouldContainExactly("Main data class names do not match: 'Boats' vs. 'Cars'. Stopping.")
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


