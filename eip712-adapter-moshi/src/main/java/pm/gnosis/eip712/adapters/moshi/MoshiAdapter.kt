package pm.gnosis.eip712.adapters.moshi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import pm.gnosis.eip712.EIP712JsonAdapter
import java.io.InputStream

class MoshiAdapter : EIP712JsonAdapter {
    private val adapter by lazy { Moshi.Builder().build().adapter(TypedData::class.java) }

    override fun parse(typedDataJson: String): EIP712JsonAdapter.Result {
        val typedData = adapter.fromJson(typedDataJson) ?: throw IllegalArgumentException("Json does not represent TypedData")
        return parse(typedData)
    }

    override fun parse(inputStream: InputStream): EIP712JsonAdapter.Result =
        inputStream.source().buffer().use { bufferedSource ->
            val typedData = adapter.fromJson(bufferedSource) ?: throw IllegalArgumentException("Json does not represent TypedData")
            return parse(typedData)
        }

    private fun parse(typedData: TypedData): EIP712JsonAdapter.Result {
        return EIP712JsonAdapter.Result(
            primaryType = typedData.primaryType,
            domain = typedData.domain,
            message = typedData.message,
            types = typedData.types.mapValues { (_, types) -> types.map { EIP712JsonAdapter.Parameter(it.name, it.type) } }
        )
    }

    @JsonClass(generateAdapter = true)
    data class TypedData(
        @Json(name = "types") val types: Map<String, List<TypeParam>>,
        @Json(name = "primaryType") val primaryType: String,
        @Json(name = "domain") val domain: Map<String, Any>,
        @Json(name = "message") val message: Map<String, Any>
    )

    @JsonClass(generateAdapter = true)
    data class TypeParam(
        @Json(name = "name") val name: String,
        @Json(name = "type") val type: String
    )
}
