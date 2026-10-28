package de.moekadu.tuner.ui

import androidx.compose.ui.geometry.Offset
import de.moekadu.tuner.ui.plot.LineCoordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class LineCoordinatesTest {

    @Test
    fun testMutateCreatesNewImmutableSnapshot() {
        val original = LineCoordinates.create(floatArrayOf(1f, 2f, 3f))
        val mutated = original.mutate(floatArrayOf(4f, 5f, 6f))

        assertEquals(3, original.size)
        assertEquals(3, mutated.size)

        // Ensure mutated is a new instance and does not share the underlying list
        assertNotSame(original.coordinates, mutated.coordinates)

        assertEquals(Offset(0f, 1f), original.coordinates[0])
        assertEquals(Offset(0f, 4f), mutated.coordinates[0])
    }

    @Test
    fun testMutateWithXYArrays() {
        val x1 = floatArrayOf(0f, 1f)
        val y1 = floatArrayOf(10f, 20f)
        val initial = LineCoordinates.create(x1, y1)

        val x2 = floatArrayOf(2f, 3f)
        val y2 = floatArrayOf(30f, 40f)
        val updated = initial.mutate(x2, y2)

        assertNotSame(initial.coordinates, updated.coordinates)
        assertEquals(Offset(0f, 10f), initial.coordinates[0])
        assertEquals(Offset(2f, 30f), updated.coordinates[0])
    }
}
