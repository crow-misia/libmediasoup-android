package io.github.zncmn.mediasoup

import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScalabilityModeTest {
    @Test
    fun testParsesCorrectly() {
        var jsonScalabilityMode = ScalabilityMode.parse("L1T3")
        assertThat(jsonScalabilityMode.spatialLayers).isEqualTo(1)
        assertThat(jsonScalabilityMode.temporalLayers).isEqualTo(3)

        jsonScalabilityMode = ScalabilityMode.parse("L30T3")
        assertThat(jsonScalabilityMode.spatialLayers).isEqualTo(30)
        assertThat(jsonScalabilityMode.temporalLayers).isEqualTo(3)

        jsonScalabilityMode = ScalabilityMode.parse("L1T6")
        assertThat(jsonScalabilityMode.spatialLayers).isEqualTo(1)
        assertThat(jsonScalabilityMode.temporalLayers).isEqualTo(6)
    }

    @Test
    fun testParsesIncorrectly() {
        val jsonScalabilityMode = ScalabilityMode.parse("1T3")
        assertThat(jsonScalabilityMode.spatialLayers).isEqualTo(1)
        assertThat(jsonScalabilityMode.temporalLayers).isEqualTo(1)
    }
}