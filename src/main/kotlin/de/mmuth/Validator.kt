package de.mmuth

typealias Violation = String

class Validator {

    fun check(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        if (againstInputClass.name != inputClass.name)
            violations.add("Main data class names do not match: '${inputClass.name}' vs. '${againstInputClass.name}'")

        violations.addAll(validateMembers(inputClass, againstInputClass))
        violations.addAll(validateReferencedTypes(inputClass, againstInputClass))

        return violations
    }

    private fun validateMembers(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        againstInputClass.members.forEach { member ->
            // all members need to exist in the input
            if (inputClass.members.none { it.name == member.name })
                violations.add("Member '${member.name}' does not exist in input class")

            // their types need to match or might be narrower considering nullability
            // "against class" can aggree with null, but "input class" will always deliver a value => is OK
            val otherMember = inputClass.members.first { it.name == member.name }
            if (member.type != otherMember.type && (!member.isNullable() && otherMember.isNullable()))
                violations.add("Member '${member.name}' types are not compatible: ${member.type} vs. ${otherMember.type}")
        }
        return violations
    }

    private fun validateReferencedTypes(
        inputClass: KotlinValidatableDataClassDescription,
        againstInputClass: KotlinValidatableDataClassDescription
    ): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        againstInputClass.referencedTypesToValidate?.forEach { typeToValidate ->
            when (typeToValidate) {
                is KotlinValidatableDataClassDescription -> {
                    val inputType = inputClass.referencedTypesToValidate?.firstOrNull { it.name == typeToValidate.name }
                    if (inputType == null)
                        violations.add("Referenced type '${typeToValidate.name}' does not exist in input class")
                    else
                        validateMembers(inputType as KotlinValidatableDataClassDescription, typeToValidate)
                }

                is KotlinEnumDescripton -> {
                    val inputType = inputClass.referencedTypesToValidate?.firstOrNull { it.name == typeToValidate.name }
                    if (inputType == null)
                        violations.add("Referenced enum '${typeToValidate.name}' does not exist in input class")
                    else
                        violations.addAll(validateEnumValues(inputType as KotlinEnumDescripton, typeToValidate))
                }
            }
        }
        return violations
    }

    private fun validateEnumValues(inputClass: KotlinEnumDescripton, againstInputClass: KotlinEnumDescripton): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        // input may not add new values that are not known to the baseline ("againstInput")
        inputClass.values.forEach { value ->
            if (againstInputClass.values.none { it == value })
                violations.add("Enum '${inputClass.name}': value '$value' is not known to the baseline")
        }
        return violations
    }

}
