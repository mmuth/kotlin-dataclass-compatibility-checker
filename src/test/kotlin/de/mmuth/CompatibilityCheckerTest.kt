package de.mmuth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import java.io.File

class CompatibilityCheckerTest : DescribeSpec({

    afterEach {
        File(INPUT_FILE_BUILD_DIR).deleteRecursively()
        File(AGAINST_INPUT_FILE_BUILD_DIR).deleteRecursively()
    }

    describe("Validation will succeed for compatible changes") {
        it("simple consumers can use the fields they require (other types are equal)") {
            val violations = validate("SimpleDataClass-Valid-AddNewFields", "SimpleDataClass-Baseline")
            violations shouldHaveSize 0
        }

        it("you may always provide data (=no null values) if the consumer accepts null") {
            val violations = validate("ClassWithLists-Valid-DeliverAlwaysValueEvenForNullableType", "ClassWithLists-Baseline")
            violations shouldHaveSize 0
        }
    }

    describe("Validation will fail for incompatible changes") {
        it("you can't remove a member that is expected on the other side") {
            val violations = validate("SimpleDataClass-Invalid-RemoveField", "SimpleDataClass-Baseline")
            violations.shouldContainExactly("Member 'count' does not exist in input class")
        }

        it("you can't deliver null if the baseline does not accept it") {
            val violations = validate("ClassWithLists-Invalid-NullForNonNullableField", "ClassWithLists-Baseline")
            violations.shouldContainExactly("Member 'count' types are not compatible: kotlin.Int? vs. kotlin.Int")
        }

        it("will fail if the types of a member differ at all") {
            val violations = validate("SimpleDataClass-Invalid-MemberTypeDoesNotMatch", "SimpleDataClass-Baseline")
            violations.shouldContainExactly("Member 'count' types are not compatible: kotlin.Long vs. kotlin.Int")
        }

        it("an enum value needs to be added to the baseline first") {
            val violations = validate("SimpleDataClassWithEnum-Invalid-AddedValueToExistingEnum", "SimpleDataClassWithEnum-Baseline")
            violations.shouldContainExactly("Enum 'Color': value 'CYAN' is not known to the baseline")
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


