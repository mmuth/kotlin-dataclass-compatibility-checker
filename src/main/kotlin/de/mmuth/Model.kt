package de.mmuth

interface KotlinValidatableTypeReference {
    val name: String
}

data class KotlinValidatableDataClassDescription(
    override val name: String,
    val classpackage: String,
    val members: List<KotlinMemberDescription>,
    val referencedTypesToValidate: Set<KotlinValidatableTypeReference>?
) : KotlinValidatableTypeReference {

    fun fullyQualifiedName() = "$classpackage.$name"

    fun isValidationSupported(): Boolean =
        members.all { it.isValidationSupported() } && getAllTypeReferences().filterIsInstance<KotlinValidatableDataClassDescription>()
            .all { it.isValidationSupported() }

    fun getAllTypeReferences(): Set<KotlinValidatableTypeReference> =
        (this.referencedTypesToValidate ?: emptySet()) +
                (this.referencedTypesToValidate?.filterIsInstance<KotlinValidatableDataClassDescription>()?.map { it.getAllTypeReferences() }?.flatten()
                    ?.toSet() ?: emptySet())
}

data class KotlinEnumDescripton(
    override val name: String,
    val values: List<String>
) : KotlinValidatableTypeReference

data class KotlinMemberDescription(
    val name: String,
    val type: String
) {
    fun isNullable() = type.contains("?")
    fun isValidationSupported() = type.count { it == '?' } <= 1
}
