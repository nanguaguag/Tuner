package de.moekadu.tuner

import de.moekadu.tuner.notedetection.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleDataTest {

    @Test
    fun testLargeFramePositionBeyondIntMax() {
        val largeFrame = 3_000_000_000L // Exceeds Int.MAX_VALUE (2_147_483_647)
        val sampleSize = 1024
        val sampleRate = 44100
        val sampleData = SampleData(sampleSize, sampleRate, largeFrame)

        assertEquals(largeFrame, sampleData.framePosition)

        // Provide input starting at largeFrame
        val input = FloatArray(512) { it.toFloat() }
        sampleData.addData(largeFrame, input)

        // First 512 values should match
        for (i in 0 until 512) {
            assertEquals(i.toFloat(), sampleData.data[i], 1e-6f)
        }

        // Add remaining 512 samples starting at largeFrame + 512
        val inputNext = FloatArray(512) { (it + 512).toFloat() }
        sampleData.addData(largeFrame + 512, inputNext)

        for (i in 0 until 1024) {
            assertEquals(i.toFloat(), sampleData.data[i], 1e-6f)
        }
        assertTrue(sampleData.isFull)
    }

    @Test
    fun testResetWithLargeFramePosition() {
        val sampleData = SampleData(256, 44100, 100L)
        val largeFrame = 5_000_000_000L
        sampleData.reset(largeFrame)
        assertEquals(largeFrame, sampleData.framePosition)
    }
}
