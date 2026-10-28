package de.moekadu.tuner

import de.moekadu.tuner.misc.MemoryPool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class MemoryPoolTest {

    private class SampleResource(val id: Int)

    @Test
    fun testBasicRecycling() {
        val pool = MemoryPool<SampleResource>(capacity = 5)
        var createdCount = 0

        val item1 = pool.get(
            factory = { SampleResource(++createdCount) },
            checker = { true }
        )
        assertEquals(1, item1.memory.id)
        assertEquals(1, createdCount)

        // Increment ref
        assertTrue(item1.incRef())
        item1.decRef() // count is 1, not recycled yet

        // Try getting another one - should allocate new because item1 is not recycled
        val item2 = pool.get(
            factory = { SampleResource(++createdCount) },
            checker = { true }
        )
        assertEquals(2, item2.memory.id)
        assertEquals(2, createdCount)

        // Now recycle item1
        item1.decRef() // count reaches 0, returned to pool
        assertFalse(item1.incRef()) // cannot incRef after recycled

        // Next get should reuse item1
        val reused = pool.get(
            factory = { SampleResource(++createdCount) },
            checker = { true }
        )
        assertEquals(1, reused.memory.id)
        assertEquals(2, createdCount) // No new allocation!
    }

    @Test
    fun testConcurrentIncRefDecRef() {
        val pool = MemoryPool<SampleResource>(capacity = 5)
        val item = pool.get(factory = { SampleResource(1) }, checker = { true })

        val numThreads = 8
        val iterations = 500
        val latch = CountDownLatch(numThreads)
        val executor = Executors.newFixedThreadPool(numThreads)

        for (t in 0 until numThreads) {
            executor.submit {
                try {
                    for (i in 0 until iterations) {
                        if (item.incRef()) {
                            item.decRef()
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // Base ref is still 1, now decRef to recycle
        item.decRef()

        // Should be recycled and ready for reuse
        val reused = pool.get(factory = { SampleResource(99) }, checker = { true })
        assertEquals(1, reused.memory.id)
    }
}
