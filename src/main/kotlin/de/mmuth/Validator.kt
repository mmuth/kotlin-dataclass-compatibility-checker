package de.mmuth

import com.github.ajalt.clikt.core.CliktError
import org.slf4j.LoggerFactory

class Validator {

    // TODO possibly gather just all violations

    private val logger = LoggerFactory.getLogger(javaClass)

    fun check(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription) {
        if (againstInputClass.name != inputClass.name)
            fail("Main data class names do not match: '${inputClass.name}' vs. '${againstInputClass.name}'")

        validateMembers(inputClass, againstInputClass)
        validateReferencedTypes(inputClass, againstInputClass)

        logger.info("Validation Succeeded!")
    }

    private fun validateMembers(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription) {
        againstInputClass.members.forEach { member ->
            // all members need to exist in the input
            if (inputClass.members.none { it.name == member.name })
                fail("Member '${member.name}' does not exist in input class")

            // their types need to match or might be narrower considering nullability
            // "against class" can aggree with null, but "input class" will always deliver a value => is OK
            val otherMember = inputClass.members.first { it.name == member.name }
            if (member.type != otherMember.type && (!member.isNullable() && otherMember.isNullable()))
                fail("Member '${member.name}' types are not compatible: ${member.type} vs. ${otherMember.type}")
        }
    }

    private fun validateReferencedTypes(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription) {
        againstInputClass.referencedTypesToValidate?.forEach { typeToValidate ->
            when (typeToValidate) {
                is KotlinValidatableDataClassDescription -> {
                    val inputType = inputClass.referencedTypesToValidate?.firstOrNull { it.name == typeToValidate.name }
                    if (inputType == null)
                        fail("Referenced type '${typeToValidate.name}' does not exist in input class")
                    else
                        validateMembers(inputType as KotlinValidatableDataClassDescription, typeToValidate)
                }

                is KotlinEnumDescripton -> {
                    val inputType = inputClass.referencedTypesToValidate?.firstOrNull { it.name == typeToValidate.name }
                    if (inputType == null)
                        fail("Referenced enum '${typeToValidate.name}' does not exist in input class")
                    else
                        validateEnumValues(inputType as KotlinEnumDescripton, typeToValidate)
                }
            }
        }
    }

    private fun validateEnumValues(inputClass: KotlinEnumDescripton, againstInputClass: KotlinEnumDescripton) {
        // input may not add new values that are not known to the baseline ("againstInput")
        inputClass.values.forEach { value ->
            if (againstInputClass.values.none { it == value })
                fail("Enum value '$value' is not known to the baseline")
        }
    }

    private fun fail(reason: String): Nothing = throw CliktError(reason)

}
