package de.mmuth

typealias Violation = String

class Validator {

    fun check(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        if (againstInputClass.name != inputClass.name)
            violations.add("Main data class names do not match: '${inputClass.name}' vs. '${againstInputClass.name}'. Stopping.")

        if (!againstInputClass.isValidationSupported() || !inputClass.isValidationSupported())
            violations.add("Sorry, inputs contain unsupported typings - currently only one nullable type per type reference is supported. Stopping.")

        if (violations.isEmpty()) {
            violations.addAll(validateMembers(inputClass, againstInputClass))
            violations.addAll(validateReferencedTypes(inputClass, againstInputClass))
        }

        return violations
    }

    private fun validateMembers(inputClass: KotlinValidatableDataClassDescription, againstInputClass: KotlinValidatableDataClassDescription): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        againstInputClass.members.forEach { member ->
            // all members need to exist in the input
            if (inputClass.members.none { it.name == member.name }) {
                violations.add("Type '${inputClass.name}', Member '${member.name}' was removed")
            } else {
                // their types need to match or might be narrower considering nullability
                // "against class" can agree with null, but "input class" will always deliver a value => is OK
                val otherMember = inputClass.members.first { it.name == member.name }
                if (typesDiffer(otherMember.type, member.type, inputClass.classpackage, againstInputClass.classpackage) &&
                    !isValidNullabilityOfSameType(otherMember, member, inputClass.classpackage, againstInputClass.classpackage)
                ) {
                    violations.add("Type '${inputClass.name}', Member '${member.name}': types are not compatible: ${otherMember.type} vs. ${member.type}")
                }
            }
        }
        return violations
    }

    private fun typesDiffer(inputMember: String, againstInputMember: String, inputPackage: String, againstInputPackage: String): Boolean {
        // rationale behind: we typically have different packages for the data classes but want to validate as if they were in the same package
        // (this should only apply for our own data classes, java and kotlin stdlib types will of course be not affected)
        return againstInputMember != inputMember.replace(inputPackage, againstInputPackage)
    }

    private fun isValidNullabilityOfSameType(
        inputMember: KotlinMemberDescription,
        againstInputMember: KotlinMemberDescription,
        inputPackage: String,
        againstInputPackage: String
    ): Boolean {
        val sameTypeIgnoringNullability = inputMember.type.replace("?", "") ==
                againstInputMember.type.replace(againstInputPackage, inputPackage).replace("?", "")

        return sameTypeIgnoringNullability && againstInputMember.isNullable() && !inputMember.isNullable()
    }

    private fun validateReferencedTypes(
        inputClass: KotlinValidatableDataClassDescription,
        againstInputClass: KotlinValidatableDataClassDescription
    ): Set<Violation> {
        val violations = mutableSetOf<Violation>()
        val inputReferences = inputClass.getAllTypeReferences()
        val againstReferences = againstInputClass.getAllTypeReferences()
        againstReferences.forEach { typeToValidate ->
            when (typeToValidate) {
                is KotlinValidatableDataClassDescription -> {
                    val inputType = inputReferences.firstOrNull { it.name == typeToValidate.name }
                    if (inputType == null)
                        violations.add("Referenced type '${typeToValidate.name}' does not exist in input class")
                    else
                        violations.addAll(validateMembers(inputType as KotlinValidatableDataClassDescription, typeToValidate))
                }

                is KotlinEnumDescripton -> {
                    val inputType = inputReferences.firstOrNull { it.name == typeToValidate.name }
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
        againstInputClass.values.forEach { value ->
            if (inputClass.values.none { it == value })
                violations.add("Enum '${inputClass.name}': value '$value' was removed")
        }
        return violations
    }

}
