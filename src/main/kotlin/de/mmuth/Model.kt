package de.mmuth

interface KotlinValidatableTypeReference {
    val name: String
}

data class KotlinValidatableClassDescription(
    override val name: String,
    val classpackage: String,
    val members: List<KotlinMemberDescription>,
    val sealedClassImplementations: Set<KotlinSealedSubClassDescription>,
    val referencedTypesToValidate: Set<KotlinValidatableTypeReference>
) : KotlinValidatableTypeReference {

    fun fullyQualifiedName() = "$classpackage.$name"

    fun getAllTypeReferences(): Set<KotlinValidatableTypeReference> =
        this.referencedTypesToValidate +
                this.referencedTypesToValidate.filterIsInstance<KotlinValidatableClassDescription>().map { it.getAllTypeReferences() }.flatten()
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

data class KotlinSealedSubClassDescription(
    override val name: String
) : KotlinValidatableTypeReference
