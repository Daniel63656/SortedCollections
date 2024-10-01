package net.scoreworks.collection

interface SortedSet<E: Comparable<E>> : MutableSet<E> {

    /**
     * Retrieves the value at the specified index in the sorted set.
     *
     * @param index the index of the value to retrieve
     * @return the value at the specified index, or `null` if the index is out of bounds
     */
    fun get(index: Int): E

    /**
     * Returns the index of the specified key in the set.
     *
     * This function searches for the given key in the set and returns its position
     * in the sorted order. If the key is not found, it returns -1.
     *
     * @param element The key to search for.
     * @return The index of the key if it exists, or -1 if the key is not present in the set.
     */
    fun indexOf(element: E): Int

    /**
     * Returns the least key strictly greater than the specified key.
     *
     * @param key the key to compare against
     * @return the higher key, or `null` if there is no such key
     */
    fun higher(element: E): E?

    /**
     * Returns the greatest key strictly less than the specified key.
     *
     * @param key the key to compare against
     * @return the lower key, or `null` if there is no such key
     */
    fun lower(element: E): E?

    /**
     * Returns the least key greater than or equal to the specified key.
     *
     * @param key the key to compare against
     * @return the ceiling key, or `null` if there is no such key
     */
    fun ceiling(element: E): E?

    /**
     * Returns the greatest key less than or equal to the specified key.
     *
     * @param key the key to compare against
     * @return the floor key, or `null` if there is no such key
     */
    fun floor(element: E): E?

    fun rangedQuery(
        start: E? = null,
        end: E? = null,
        inclusive: Pair<Boolean, Boolean> = Pair(true, true),
        reverse: Boolean = false
    ): Iterator<E>
}
