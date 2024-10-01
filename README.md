# SortedCollections

This is an implementation of sorted collections like set and map in pure Kotlin multiplatform. These collections
are implemented using **Order Statistic Trees**, which allows for efficient retrieval by key **and** index. This way,
these classes provide index based access to keys and values, which Javas TreeMap can't do for example.

The proposed SortedSet and SortedMap interfaces also provide functions to navigate the collections like lower, higher, ceiling and
floor. Instead of providing half a dozen different sub set/map-views like the Java counterparts, the interfaces
provide a single **rangedQuery** function that can be called with optional start, end, border and reverse parameters,
similar to the irange function of
Python's [sortedcollections](https://pypi.org/project/sortedcollections/#:~:text=Sorted%20Collections%20is%20an%20Apache2)
package.

All the code is implemented in pure Kotlin, avoiding the use of Java packages. This design choice ensures compatibility
with Kotlin Multiplatform projects and facilitates that the implementation is fast on all compiled targets.

The implementation is based on [this](https://github.com/coderodde/OrderStatisticTree/tree/master) Java code for Order
Statistic Trees.

Coming from Java collections, I always hated that TreeMaps and Sets can not be accessed with indices, despite this function
being easily addable by modifying TreeMap's internal tree structure. However, these structures are private and TreeMap
final. As Kotlin lacks an official implementation for sorted collections, I thought I should give it a shot. However, it
seems unlikely that this would be added to the official language standard, as Kotlin Multiplatform seems to delegate
concrete implementations of collections to the individual targets through the use of the expect/actual paradigm. 

## WARNING

This repo is currently untested. Contributions are welcome.
