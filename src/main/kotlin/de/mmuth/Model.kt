package de.mmuth

interface KotlinValidatableTypeReference {
    val name: String
}

data class KotlinValidatableDataClassDescription(
    override val name: String,
    val classpackage: String,
    val members: List<KotlinMemberDescription>,
    val referencedTypesToValidate: Set<KotlinValidatableTypeReference>
) : KotlinValidatableTypeReference {

    fun fullyQualifiedName() = "$classpackage.$name"

    fun getAllTypeReferences(): Set<KotlinValidatableTypeReference> =
        this.referencedTypesToValidate +
                this.referencedTypesToValidate.filterIsInstance<KotlinValidatableDataClassDescription>().map { it.getAllTypeReferences() }.flatten()
                    .toSet()
}

data class KotlinEnumDescripton(
    override val name: String,
    val values: List<String>
) : KotlinValidatableTypeReference

data class KotlinMemberDescription(
    val name: String,
    val type: String
)
