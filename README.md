# SortedCollections

This is an implementation of sorted collections like set and map in pure Kotlin. These collections
are implemented using **Order Statistic Trees**, which allows for efficient retrieval by key **and** index. This way,
these classes provide additional index based access to keys and values, unlike Java's sorted collections.

The proposed SortedSet and SortedMap interfaces also provide functions to navigate the collections like lower, higher, ceiling and
floor. Instead of providing half a dozen different sub set/map-views like the Java counterparts, the interfaces
provide a single **rangedQuery** function that can be called with optional start, end, inclusive and reverse parameters,
similar to the **irange** function of
Python's [sortedcollections](https://pypi.org/project/sortedcollections/#:~:text=Sorted%20Collections%20is%20an%20Apache2)
package.

All the code is implemented in pure Kotlin, and can thus be used in Kotlin Multiplatform projects, making the implementation fast on all compiled targets.

The implementation is based on [this](https://github.com/coderodde/OrderStatisticTree/tree/master) Java code for Order
Statistic Trees.

Kotlin lacks an official implementation for sorted collections. However, it
seems unlikely that this code would be added to the official language standard, as it is common for Kotlin Multiplatform code
to delegate concrete implementations of collections to the individual targets through the use of the expect/actual paradigm. 

## WARNING

While these collections have been **practically** tested by using them within other projects, the repository currently
lacks proper test coverage. Use with caution. Contributions are welcome.
