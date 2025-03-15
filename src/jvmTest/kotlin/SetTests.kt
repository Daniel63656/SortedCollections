import net.scoreworks.collection.MutableSortedMap
import net.scoreworks.collection.MutableSortedSet
import net.scoreworks.collection.TreeMap
import net.scoreworks.collection.TreeSet
import kotlin.test.Test

class SetTests {

    @Test
    fun testFloorAndLower() {
        val set: MutableSortedSet<Int> = TreeSet()
        set.add(1)
        set.add(3)

        assert(set.lower(1) == null)
        assert(set.floor(1) == 1)
        assert(set.lower(3) == 1)
        assert(set.floor(3) == 3)
    }

    @Test
    fun testCeilingAndHigher() {
        val set: MutableSortedSet<Int> = TreeSet()
        set.add(1)
        set.add(3)

        assert(set.higher(3) == null)
        assert(set.ceiling(3) == 3)
        assert(set.higher(1) == 3)
        assert(set.ceiling(1) == 1)
    }
}
