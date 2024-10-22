package net.scoreworks.collection


interface SortedMap<K: Comparable<K>, V> : Map<K, V> {

    /**
     * Retrieves the value at the specified index in the sorted map.
     *
     * @param index the index of the value to retrieve
     * @return the value at the specified index, or `null` if the index is out of bounds
     */
    fun get(index: Int): V?

    /**
     * Returns the index of the specified key in this map.
     *
     * This function searches for the given key in the map and returns its position
     * in the sorted order. If the key is not found, it returns -1.
     *
     * @param key The key to search for.
     * @return The index of the key if it exists, or -1 if the key is not present in the map.
     */
    fun indexOf(key: K): Int

    /**
     * Returns the least key strictly greater than the specified key.
     *
     * @param key the key to compare against
     * @return the higher key, or `null` if there is no such key
     */
    fun higherKey(key: K): K?

    /**
     * Returns the entry with the least key strictly greater than the specified key.
     *
     * @param key the key to compare against
     * @return the entry with the higher key, or `null` if there is no such entry
     */
    fun higherEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns the greatest key strictly less than the specified key.
     *
     * @param key the key to compare against
     * @return the lower key, or `null` if there is no such key
     */
    fun lowerKey(key: K): K?

    /**
     * Returns the entry with the greatest key strictly less than the specified key.
     *
     * @param key the key to compare against
     * @return the entry with the lower key, or `null` if there is no such entry
     */
    fun lowerEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns the least key greater than or equal to the specified key.
     *
     * @param key the key to compare against
     * @return the ceiling key, or `null` if there is no such key
     */
    fun ceilingKey(key: K): K?

    /**
     * Returns the entry with the least key greater than or equal to the specified key.
     *
     * @param key the key to compare against
     * @return the entry with the ceiling key, or `null` if there is no such entry
     */
    fun ceilingEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns the greatest key less than or equal to the specified key.
     *
     * @param key the key to compare against
     * @return the floor key, or `null` if there is no such key
     */
    fun floorKey(key: K): K?

    /**
     * Returns the entry with the greatest key less than or equal to the specified key.
     *
     * @param key the key to compare against
     * @return the entry with the floor key, or `null` if there is no such entry
     */
    fun floorEntry(key: K): Map.Entry<K, V>?

    /**
     * Returns the first key in the map or `null` if map is empty.
     */
    fun firstKey(): K?

    /**
     * Returns the first entry in the map or `null` if map is empty.
     */
    fun firstEntry(): Map.Entry<K, V>?

    /**
     * Returns the last key in the map or `null` if map is empty.
     */
    fun lastKey(): K?

    /**
     * Returns the last entry in the map or `null` if map is empty.
     */
    fun lastEntry(): Map.Entry<K, V>?

    /**
     * Creates an iterator of values between `start` and `end`.
     *
     * Both `start` and `end` default to `null`, which includes all entries
     * from the beginning to the end of the map.
     *
     * The `inclusive` parameter is a pair of booleans indicating whether the
     * `start` and `end` values should be included in the range,
     * respectively. The default is `(true, true)`, making the range inclusive
     * of both `start` and `end`.
     *
     * When `reverse` is `true`, the values are yielded from the iterator in
     * reverse order; `reverse` defaults to `false`.
     */
    fun rangedQuery(
        start: K? = null,
        end: K? = null,
        inclusive: Pair<Boolean, Boolean> = Pair(true, true),
        reverse: Boolean = false
    ): Iterator<Map.Entry<K, V>>
}
