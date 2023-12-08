package sources.requests

import sources.annotations.KtorDiscriminator

data class SimpleRequest(
    val string: String,
    val integer: Int,
    val float: Float
)

data class NestedRequest(
    val nestedObject: SimpleRequest
)

data class ComplexRequest(
    val evenMore: Int?,
    val list: List<String>,
    val nestedList: List<List<String>>,
    val nestedMutableList: MutableList<List<List<List<String>>>>,
    val complexList: List<ComplexMapValue>,
    val complexNestedList: List<List<ComplexMapValue>>,
    val complexListStringMap: Map<String, List<String>>,
    val complexListMap: Map<String, List<ComplexMapValue>>,
    val complexNestedListMap: Map<String, List<List<ComplexMapValue>>>,
    val stringMap: Map<String, String>,
    val intValueMap: Map<String, Int>,
    val complexValueMap: Map<String, ComplexMapValue>,
    val enumValueMap: Map<String, MyEnum>,
    val complexEnumValueMap: Map<String, List<MyEnum>>,
)

data class ComplexMapValue(
    val something: String
)

data class ComplexMapKey(
    val something: Int
)

enum class MyEnum {
    ONE,
    TWO,
    THREE
}

enum class PolyDiscriminator {
    IMPL1,
    IMPL2
}

data class SimplePolymorphicRequest(
    val req: PolyRequest
)
sealed interface PolyRequest{
    @KtorDiscriminator(["IMPL1", "IMPL2"], [PolyRequestImpl::class, PolyRequestImpl2::class])
    val discriminator: PolyDiscriminator
}

data class PolyRequestImpl(
    override val discriminator: PolyDiscriminator,
    val string: String
): PolyRequest

data class PolyRequestImpl2(
    override val discriminator: PolyDiscriminator,
    val integer: Int
): PolyRequest