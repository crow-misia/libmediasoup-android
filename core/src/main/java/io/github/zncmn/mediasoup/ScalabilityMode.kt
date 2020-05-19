package io.github.zncmn.mediasoup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScalabilityMode(
    val spatialLayers: Int,
    val temporalLayers: Int
) {
    companion object {
        private val PATTERN = "^[LS]([1-9]\\d{0,1})T([1-9]\\d{0,1})".toPattern()

        @JvmStatic
        fun parse(text: String): ScalabilityMode {
            var spatialLayers = 1
            var temporalLayers = 1

            val matcher = PATTERN.matcher(text)
            if (matcher.matches()) {
                spatialLayers = matcher.group(1)?.toIntOrNull() ?: 1
                temporalLayers = matcher.group(2)?.toIntOrNull() ?: 1
            }
            return ScalabilityMode(
                spatialLayers,
                temporalLayers
            )
        }
    }
}