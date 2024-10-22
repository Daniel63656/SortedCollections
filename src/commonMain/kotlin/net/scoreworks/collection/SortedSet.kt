package net.scoreworks.collection


interface SortedSet<E: Comparable<E>> : Set<E> {

    /**
     * Retrieves the value at the specified index in the sorted set.
     *
     * @param index the index of the value to retrieve
     * @return the value at the specified index, or `null` if the index is out of bounds
     */
    fun get(index: Int): E

    /**
     * Returns the index of the specified element in the set.
     *
     * This function searches for the given element in the set and returns its position
     * in the sorted order. If the element is not found, it returns -1.
     *
     * @param element The element to search for.
     * @return The index of the element if it exists, or -1 if the element is not present in the set.
     */
    fun indexOf(element: E): Int

    /**
     * Returns the least element strictly greater than the specified element.
     *
     * @param element the element to compare against
     * @return the higher element, or `null` if there is no such element
     */
    fun higher(element: E): E?

    /**
     * Returns the greatest element strictly less than the specified element.
     *
     * @param element the element to compare against
     * @return the lower element, or `null` if there is no such element
     */
    fun lower(element: E): E?

    /**
     * Returns the least element greater than or equal to the specified element.
     *
     * @param element the element to compare against
     * @return the ceiling element, or `null` if there is no such element
     */
    fun ceiling(element: E): E?

    /**
     * Returns the greatest element less than or equal to the specified element.
     *
     * @param element the element to compare against
     * @return the floor element, or `null` if there is no such element
     */
    fun floor(element: E): E?

    /**
     * Returns the first element in the set or `null` if set is empty.
     */
    fun first(): E?

    /**
     * Returns the last element in the set or `null` if set is empty.
     */
    fun last(): E?

    fun rangedQuery(
        start: E? = null,
        end: E? = null,
        inclusive: Pair<Boolean, Boolean> = Pair(true, true),
        reverse: Boolean = false
    ): Iterator<E>
}
