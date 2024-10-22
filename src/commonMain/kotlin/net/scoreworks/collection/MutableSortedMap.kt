package net.scoreworks.collection


interface MutableSortedMap<K : Comparable<K>, V> : SortedMap<K, V>, MutableMap<K, V>
