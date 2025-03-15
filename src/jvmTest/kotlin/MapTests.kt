import net.scoreworks.collection.MutableSortedMap
import net.scoreworks.collection.TreeMap
import kotlin.test.Test

class MapTests {

    @Test
    fun testFloorAndLower() {
        val map: MutableSortedMap<Int, String> = TreeMap()
        map[1] = "one"
        map[3] = "three"

        assert(map.lowerKey(1) == null)
        assert(map.lowerEntry(1) == null)
        assert(map.floorKey(1) == 1)
        assert(map.floorEntry(1)!!.value == "one")
        assert(map.lowerKey(3) == 1)
        assert(map.lowerEntry(3)!!.value == "one")
        assert(map.floorKey(3) == 3)
        assert(map.floorEntry(3)!!.value == "three")
    }

    @Test
    fun testCeilingAndHigher() {
        val map: MutableSortedMap<Int, String> = TreeMap()
        map[1] = "one"
        map[3] = "three"

        assert(map.higherKey(3) == null)
        assert(map.higherEntry(3) == null)
        assert(map.ceilingKey(3) == 3)
        assert(map.ceilingEntry(3)!!.value == "three")
        assert(map.higherKey(1) == 3)
        assert(map.higherEntry(1)!!.value == "three")
        assert(map.ceilingKey(1) == 1)
        assert(map.ceilingEntry(1)!!.value == "one")
    }
}
