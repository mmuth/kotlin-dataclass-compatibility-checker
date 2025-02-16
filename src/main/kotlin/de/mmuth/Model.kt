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
}

data class KotlinEnumDescripton(
    override val name: String,
    val values: List<String>
) : KotlinValidatableTypeReference

data class KotlinMemberDescription(
    val name: String,
    val type: String
) {
    fun isNullable() = type.contains("?") // TODO this a bit too easy currently as it could be wrapped in Lists/Maps...
}
