package net.scoreworks.collection


open class TreeMap<K : Comparable<K>, V> : MutableSortedMap<K, V> {
    private var root: Node<K, V>? = null
    private var _size: Int = 0  // Internal size variable
    private var modCount = 0    // For concurrency

    private class Node<K, V>(override var key: K, override var value: V): MutableMap.MutableEntry<K, V> {
        var parent: Node<K, V>? = null
        var left: Node<K, V>? = null
        var right: Node<K, V>? = null
        var height = 0
        var count = 0
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            return old
        }
    }

    override val size: Int get() = _size

    override fun clear() {
        modCount += _size
        root = null
        _size = 0
    }

    override fun containsValue(value: V): Boolean {
        if (root == null) return false
        var node: Node<K, V>? = minimumNode(root!!)
        while (node != null) {
            if (node.value == value) {
                return true
            }
            node = successor(node)
        }
        return false
    }

    override fun isEmpty(): Boolean {
        return _size == 0
    }

    override fun put(key: K, value: V): V? {
        if (root == null) {
            root = Node(key, value)
            _size = 1
            modCount++
            return null
        }
        var parent: Node<K, V>? = null
        var node = root
        var cmp: Int
        while (node != null) {
            cmp = key.compareTo(node.key)
            if (cmp == 0) {
                // The key is already in this tree. Override and return old value
                val oldValue = node.value
                node.value = value
                return oldValue
            }
            parent = node
            node = if (cmp < 0) node.left else node.right
        }
        val newNode = Node(key, value)
        if (key < parent!!.key) parent.left = newNode else parent.right = newNode
        newNode.parent = parent
        _size++
        modCount++
        var hi = parent
        var lo = newNode
        while (hi != null) {
            if (hi.left == lo) {
                hi.count++
            }
            lo = hi
            hi = hi.parent
        }
        fixAfterModification(newNode, true)
        return null
    }

    override fun putAll(from: Map<out K, V>) {
        for (key in from) {
            put(key.key, key.value)
        }
    }

    override fun containsKey(key: K): Boolean {
        return getNode(key) != null
    }

    override fun get(key: K): V? {
        return getNode(key)?.value
    }

    override operator fun get(index: Int): V {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("The input index is out of bounds.")
        }
        var idx = index
        // Now root should exist!
        var node: Node<K, V> = root!!

        while (true) {
            if (index > node.count) {
                idx -= node.count + 1
                node = node.right!!
            } else if (index < node.count) {
                node = node.left!!
            } else {
                return node.value
            }
        }
    }

    override fun indexOf(key: K): Int {
        var node: Node<K, V>? = root
        if (root == null) {
            return -1
        }
        var rank = root!!.count
        var cmp: Int
        while (node != null) {
            if (key.compareTo(node.key).also { cmp = it } < 0) {
                if (node.left == null) {
                    return -1
                }
                rank -= node.count - node.left!!.count
                node = node.left
            } else if (cmp > 0) {
                if (node.right == null) {
                    return -1
                }
                rank += 1 + node.right!!.count
                node = node.right
            } else {
                break
            }
        }
        return if (node == null) -1 else rank
    }

    override fun remove(key: K): V? {
        var x = getNode(key) ?: return null
        x = deleteNode(x)
        fixAfterModification(x, false)
        _size--
        modCount++
        return x.value
    }

    override fun higherKey(key: K): K? {
        return higherNode(key)?.key
    }

    override fun higherEntry(key: K): Map.Entry<K, V>? {
        return higherNode(key)
    }

    override fun lowerKey(key: K): K? {
        return lowerNode(key)?.key
    }

    override fun lowerEntry(key: K): Map.Entry<K, V>? {
        return lowerNode(key)
    }

    override fun ceilingKey(key: K): K? {
        if (contains(key)) return key
        return higherNode(key)?.key
    }

    override fun ceilingEntry(key: K): Map.Entry<K, V>? {
        val node = getNode(key)
        return node ?: higherEntry(key)
    }

    override fun floorKey(key: K): K? {
        if (contains(key)) return key
        return lowerNode(key)?.key
    }

    override fun floorEntry(key: K): Map.Entry<K, V>? {
        val node = getNode(key)
        return node ?: lowerNode(key)
    }

    override fun firstKey(): K? {
        return root?.let { minimumNode(it).key }
    }

    override fun firstEntry(): Map.Entry<K, V>? {
        return root?.let { maximumNode(it) }
    }

    override fun lastKey(): K? {
        return root?.let { maximumNode(it).key }
    }

    override fun lastEntry(): Map.Entry<K, V>? {
        return root?.let { maximumNode(it) }
    }

    override fun rangedQuery(start: K?, end: K?, startInclusive: Boolean, endInclusive: Boolean, reverse: Boolean): Iterator<Map.Entry<K, V>> {
        if (root == null) {
            // Is empty; return an empty iterator
            return emptyMap<K, V>().iterator()
        }
        if (!reverse) {
            // Determine the start node for iteration
            val startNode: Node<K, V>? = if (start != null) {
                if (startInclusive) {
                    val node = getNode(start)
                    node ?: higherNode(start)
                } else {
                    higherNode(start)
                }
            } else {
                minimumNode(root!!)
            }
            // End point (not a node)
            val stop: K = end ?: maximumNode(root!!).key

            return object : Iterator<Map.Entry<K, V>> {
                private var nextNode: Node<K, V>? = startNode
                private var expectedModCount: Int = modCount    // detect concurrent modifications

                override fun hasNext(): Boolean {
                    return nextNode != null && ((endInclusive && nextNode!!.key <= stop) || (!endInclusive && nextNode!!.key < stop))
                }

                override fun next(): Map.Entry<K, V> {
                    val currentNode = nextNode ?: throw NoSuchElementException("Iteration exceeded.")
                    if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                    nextNode = successor(currentNode)
                    return currentNode
                }
            }
        }
        else {
            // Determine the start node for reverse iteration
            val startNode: Node<K, V>? = if (end != null) {
                if (endInclusive) {
                    val node = getNode(end)
                    node ?: lowerNode(end)
                } else {
                    lowerNode(end)
                }
            } else {
                maximumNode(root!!)
            }

            // Define the stop point
            val stop: K = start ?: minimumNode(root!!).key

            return object : Iterator<Map.Entry<K, V>> {
                private var nextNode: Node<K, V>? = startNode
                private var expectedModCount: Int = modCount // detect concurrent modifications

                override fun hasNext(): Boolean {
                    return nextNode != null && ((startInclusive && nextNode!!.key >= stop) || (!startInclusive && nextNode!!.key > stop))
                }

                override fun next(): Map.Entry<K, V> {
                    val currentNode = nextNode ?: throw NoSuchElementException("Iteration exceeded.")
                    if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                    nextNode = predecessor(currentNode)
                    return currentNode
                }
            }
        }
    }

    // ==================== Map views ==================== //

    override val keys: MutableSet<K> by lazy {
        object : MutableSet<K> {
            override val size: Int
                get() = this@TreeMap._size

            override fun clear() {
                this@TreeMap.clear()
            }

            override fun isEmpty(): Boolean {
                return this@TreeMap.isEmpty()
            }

            override fun iterator(): MutableIterator<K> {
                return object : MutableIterator<K> {
                    private var previousNode: Node<K, V>? = null
                    private var nextNode: Node<K, V>? = null
                    private var expectedModCount: Int = modCount    // detect concurrent modifications

                    init {
                        nextNode = root?.let { minimumNode(it) }
                    }

                    override fun hasNext(): Boolean {
                        return nextNode != null
                    }

                    override fun next(): K {
                        if (nextNode == null) {
                            throw NoSuchElementException("Iteration exceeded.")
                        }
                        if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                        val datum: K = nextNode!!.key
                        previousNode = nextNode
                        nextNode = successor(nextNode!!)
                        return datum
                    }

                    override fun remove() {
                        checkNotNull(previousNode) { if (nextNode == null) "Not a single call to next(); nothing to remove." else "Removing the same key twice." }
                        if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                        val x: Node<K, V> = deleteNode(previousNode!!)
                        fixAfterModification(x, false)
                        if (x == nextNode) {
                            nextNode = previousNode
                        }
                        expectedModCount = ++modCount
                        _size--
                        previousNode = null
                    }
                }
            }

            override fun retainAll(elements: Collection<K>): Boolean {
                // Create a HashSet from the elements if it is not already a HashSet
                val elementSet = if (elements is HashSet<*>) elements as HashSet<K> else HashSet(elements)
                // Use the iterator from the inner class
                val iterator = iterator()
                var modified = false
                // Iterate over the elements using the iterator
                while (iterator.hasNext()) {
                    val element = iterator.next()
                    if (!elementSet.contains(element)) {
                        iterator.remove()
                        modified = true
                    }
                }
                return modified
            }

            override fun removeAll(elements: Collection<K>): Boolean {
                var modified = false
                for (element in elements) {
                    if (this@TreeMap.remove(element) == null) {
                        modified = true
                    }
                }
                return modified
            }

            override fun remove(element: K): Boolean {
                return this@TreeMap.remove(element) == null
            }

            override fun containsAll(elements: Collection<K>): Boolean {
                for (element in elements) {
                    if (!this@TreeMap.contains(element)) {
                        return false
                    }
                }
                return true
            }

            override fun contains(element: K): Boolean {
                return this@TreeMap.contains(element)
            }

            override fun add(element: K): Boolean {
                throw UnsupportedOperationException()
            }

            override fun addAll(elements: Collection<K>): Boolean {
                throw UnsupportedOperationException()
            }
        }
    }

    override val values: MutableCollection<V> by lazy {
        object : MutableCollection<V> {
            override val size: Int
                get() = this@TreeMap._size

            override fun clear() {
                this@TreeMap.clear()
            }

            override fun isEmpty(): Boolean {
                return this@TreeMap.isEmpty()
            }

            override fun iterator(): MutableIterator<V> {
                return object : MutableIterator<V> {
                    private var previousNode: Node<K, V>? = null
                    private var nextNode: Node<K, V>? = null
                    private var expectedModCount: Int = modCount    // detect concurrent modifications

                    init {
                        nextNode = root?.let { minimumNode(it) }
                    }

                    override fun hasNext(): Boolean {
                        return nextNode != null
                    }

                    override fun next(): V {
                        if (nextNode == null) {
                            throw NoSuchElementException("Iteration exceeded.")
                        }
                        if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                        val datum: V = nextNode!!.value
                        previousNode = nextNode
                        nextNode = successor(nextNode!!)
                        return datum
                    }

                    override fun remove() {
                        checkNotNull(previousNode) { if (nextNode == null) "Not a single call to next(); nothing to remove." else "Removing the same key twice." }
                        if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                        val x: Node<K, V> = deleteNode(previousNode!!)
                        fixAfterModification(x, false)
                        if (x == nextNode) {
                            nextNode = previousNode
                        }
                        expectedModCount = ++modCount
                        _size--
                        previousNode = null
                    }
                }
            }

            override fun retainAll(elements: Collection<V>): Boolean {
                // Create a HashSet from the elements if it is not already a HashSet
                val elementSet = if (elements is HashSet<*>) elements as HashSet<V> else HashSet(elements)
                // Use the iterator from the inner class
                val iterator = iterator()
                var modified = false
                // Iterate over the elements using the iterator
                while (iterator.hasNext()) {
                    val element = iterator.next()
                    if (!elementSet.contains(element)) {
                        iterator.remove()
                        modified = true
                    }
                }
                return modified
            }

            override fun removeAll(elements: Collection<V>): Boolean {
                var modified = false
                for (element in elements) {
                    if (remove(element)) {
                        modified = true
                    }
                }
                return modified
            }

            override fun remove(element: V): Boolean {
                var e: Node<K, V>? = minimumNode(root!!)
                while (e != null) {
                    if (valEquals(e.value, element)) {
                        this@TreeMap.remove(e.key)
                        return true
                    }
                    e = successor(e)
                }
                return false
            }

            override fun containsAll(elements: Collection<V>): Boolean {
                for (element in elements) {
                    if (!this@TreeMap.containsValue(element)) {
                        return false
                    }
                }
                return true
            }

            override fun contains(element: V): Boolean {
                return this@TreeMap.containsValue(element)
            }

            override fun addAll(elements: Collection<V>): Boolean {
                throw UnsupportedOperationException()
            }

            override fun add(element: V): Boolean {
                throw UnsupportedOperationException()
            }

            private fun valEquals(o1: V?, o2: V?): Boolean {
                return (if (o1 == null) o2 == null else (o1 == o2))
            }
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> by lazy {
        object : MutableSet<MutableMap.MutableEntry<K, V>> {
            override val size: Int
                get() = this@TreeMap._size

            override fun clear() {
                this@TreeMap.clear()
            }

            override fun isEmpty(): Boolean {
                return this@TreeMap.isEmpty()
            }

            override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> {
                return object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                    private var previousNode: Node<K, V>? = null
                    private var nextNode: Node<K, V>? = null
                    private var expectedModCount: Int = modCount    // detect concurrent modifications

                    init {
                        nextNode = root?.let { minimumNode(it) }
                    }

                    override fun hasNext(): Boolean {
                        return nextNode != null
                    }

                    override fun next(): MutableMap.MutableEntry<K, V> {
                        if (nextNode == null) {
                            throw NoSuchElementException("Iteration exceeded.")
                        }
                        if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                        val datum: MutableMap.MutableEntry<K, V> = nextNode!!
                        previousNode = nextNode
                        nextNode = successor(nextNode!!)
                        return datum
                    }

                    override fun remove() {
                        checkNotNull(previousNode) { if (nextNode == null) "Not a single call to next(); nothing to remove." else "Removing the same key twice." }
                        if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                        val x: Node<K, V> = deleteNode(previousNode!!)
                        fixAfterModification(x, false)
                        if (x == nextNode) {
                            nextNode = previousNode
                        }
                        expectedModCount = ++modCount
                        _size--
                        previousNode = null
                    }
                }
            }

            override fun retainAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                // Create a HashSet from the elements if it is not already a HashSet
                val elementSet = if (elements is HashSet<*>) elements as HashSet<MutableMap.MutableEntry<K, V>> else HashSet(elements)
                // Use the iterator from the inner class
                val iterator = iterator()
                var modified = false
                // Iterate over the elements using the iterator
                while (iterator.hasNext()) {
                    val element = iterator.next()
                    if (!elementSet.contains(element)) {
                        iterator.remove()
                        modified = true
                    }
                }
                return modified
            }

            override fun remove(element: MutableMap.MutableEntry<K, V>): Boolean {
                return this@TreeMap.remove(element.key) != null
            }

            override fun removeAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                var modified = false
                for (element in elements) {
                    if (this@TreeMap.remove(element.key) == null) {
                        modified = true
                    }
                }
                return modified
            }

            override fun contains(element: MutableMap.MutableEntry<K, V>): Boolean {
                return this@TreeMap.contains(element.key)
            }

            override fun containsAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                for (element in elements) {
                    if (!this@TreeMap.contains(element.key)) {
                        return false
                    }
                }
                return true
            }

            override fun add(element: MutableMap.MutableEntry<K, V>): Boolean {
                return this@TreeMap.put(element.key, element.value) == null
            }

            override fun addAll(elements: Collection<MutableMap.MutableEntry<K, V>>): Boolean {
                var modified = false
                for (element in elements) {
                    if (this@TreeMap.put(element.key, element.value) == null) {
                        modified = true
                    }
                }
                return modified
            }
        }
    }

    // ==================== Internal functions ==================== //

    private fun getNode(key: K): Node<K, V>? {
        var x = root
        var cmp = 0
        while (x != null && key.compareTo(x.key).also { cmp = it } != 0) {
            x = if (cmp < 0) x.left else x.right
        }
        return x
    }

    private fun successor(node: Node<K, V>): Node<K, V>? {
        var n: Node<K, V> = node
        if (n.right != null) {
            n = n.right!!
            while (n.left != null) {
                n = n.left!!
            }
            return n
        }
        var parent: Node<K, V>? = n.parent
        while (parent != null && parent.right == n) {
            n = parent
            parent = parent.parent
        }
        return parent
    }

    private fun predecessor(node: Node<K, V>): Node<K, V>? {
        var n: Node<K, V> = node
        if (n.left != null) {
            n = n.left!!
            while (n.right != null) {
                n = n.right!!
            }
            return n
        }
        var parent: Node<K, V>? = n.parent
        while (parent != null && parent.left == n) {
            n = parent
            parent = parent.parent
        }
        return parent
    }

    private fun higherNode(key: K): Node<K, V>? {
        var node = root
        var successor: Node<K, V>? = null
        while (node != null) {
            if (key < node.key) {
                successor = node // Current node is a candidate
                node = node.left
            } else {
                node = node.right
            }
        }
        return successor
    }

    private fun lowerNode(key: K): Node<K, V>? {
        var node = root
        var candidate: Node<K, V>? = null
        while (node != null) {
            if (key <= node.key) {
                node = node.left
            } else {
                candidate = node // Current node is a candidate
                node = node.right
            }
        }
        return candidate
    }

    private fun minimumNode(node: Node<K, V>): Node<K, V> {
        var n: Node<K, V> = node
        while (n.left != null) {
            n = n.left!!
        }
        return n
    }

    private fun maximumNode(node: Node<K, V>): Node<K, V> {
        var n: Node<K, V> = node
        while (n.right != null) {
            n = n.right!!
        }
        return n
    }

    private fun deleteNode(node: Node<K, V>): Node<K, V> {
        if (node.left == null && node.right == null) {
            // 'node' has no children.
            val parent: Node<K, V>? = node.parent
            if (parent == null) {
                // 'node' is the root node of this tree.
                root = null
                ++modCount
                return node
            }
            var lo: Node<K, V>? = node
            var hi: Node<K, V>? = parent
            while (hi != null) {
                if (hi.left == lo) {
                    hi.count--
                }
                lo = hi
                hi = hi.parent
            }
            if (node == parent.left) {
                parent.left = null
            } else {
                parent.right = null
            }
            return node
        }
        if (node.left != null && node.right != null) {
            // 'node' has both children.
            val tmpKey: K = node.key
            val successor: Node<K, V> = minimumNode(node.right!!)
            node.key = successor.key
            val child: Node<K, V>? = successor.right
            val parent: Node<K, V> = successor.parent!!
            if (parent.left == successor) {
                parent.left = child
            } else {
                parent.right = child
            }
            if (child != null) {
                child.parent = parent
            }
            var lo: Node<K, V>? = child
            var hi: Node<K, V>? = parent
            while (hi != null) {
                if (hi.left == lo) {
                    hi.count--
                }
                lo = hi
                hi = hi.parent
            }
            successor.key = tmpKey
            return successor
        }

        // 'node' has only one child.
        val child: Node<K, V> = if (node.left != null) node.left!! else node.right!!
        val parent: Node<K, V>? = node.parent
        child.parent = parent
        if (parent == null) {
            root = child
            return node
        }
        if (node == parent.left) {
            parent.left = child
        } else {
            parent.right = child
        }
        var hi: Node<K, V>? = parent
        var lo: Node<K, V> = child
        while (hi != null) {
            if (hi.left == lo) {
                hi.count--
            }
            lo = hi
            hi = hi.parent
        }
        return node
    }

    private fun height(node: Node<K, V>?): Int {
        return node?.height ?: -1
    }

    private fun leftRotate(node1: Node<K, V>): Node<K, V> {
        val node2: Node<K, V> = node1.right!!
        node2.parent = node1.parent
        node1.parent = node2
        node1.right = node2.left
        node2.left = node1
        if (node1.right != null) {
            node1.right!!.parent = node1
        }
        node1.height = maxOf(height(node1.left), height(node1.right)) + 1
        node2.height = maxOf(height(node2.left), height(node2.right)) + 1
        node2.count += node1.count + 1
        return node2
    }

    private fun rightRotate(node1: Node<K, V>): Node<K, V> {
        val node2: Node<K, V> = node1.left!!
        node2.parent = node1.parent
        node1.parent = node2
        node1.left = node2.right
        node2.right = node1
        if (node1.left != null) {
            node1.left!!.parent = node1
        }
        node1.height = maxOf(height(node1.left), height(node1.right)) + 1
        node2.height = maxOf(height(node2.left), height(node2.right)) + 1
        node1.count -= node2.count + 1
        return node2
    }

    private fun rightLeftRotate(node1: Node<K, V>): Node<K, V> {
        val node2: Node<K, V> = node1.right!!
        node1.right = rightRotate(node2)
        return leftRotate(node1)
    }

    private fun leftRightRotate(node1: Node<K, V>): Node<K, V> {
        val node2: Node<K, V> = node1.left!!
        node1.left = leftRotate(node2)
        return rightRotate(node1)
    }

    private fun fixAfterModification(node: Node<K, V>, insertionMode: Boolean) {
        // If insertionMode is false, this fixes a deletion instead
        var parent: Node<K, V>? = node.parent
        var grandParent: Node<K, V>?
        var subTree: Node<K, V>
        while (parent != null) {
            if (height(parent.left) == height(parent.right) + 2) {
                grandParent = parent.parent
                subTree = if (height(parent.left?.left) >= height(parent.left?.right)) {
                    rightRotate(parent)
                } else {
                    leftRightRotate(parent)
                }
                if (grandParent == null) {
                    root = subTree
                } else if (grandParent.left == parent) {
                    grandParent.left = subTree
                } else {
                    grandParent.right = subTree
                }
                if (grandParent != null) {
                    grandParent.height = maxOf(
                        height(grandParent.left),
                        height(grandParent.right)
                    ) + 1
                }
                if (insertionMode) {
                    // Whenever fixing after insertion, at most one rotation is
                    // required in order to maintain the balance.
                    return
                }
            } else if (height(parent.right) == height(parent.left) + 2) {
                grandParent = parent.parent
                subTree = if (height(parent.right?.right) >= height(parent.right?.left)) {
                    leftRotate(parent)
                } else {
                    rightLeftRotate(parent)
                }
                if (grandParent == null) {
                    root = subTree
                } else if (grandParent.left == parent) {
                    grandParent.left = subTree
                } else {
                    grandParent.right = subTree
                }
                if (grandParent != null) {
                    grandParent.height = maxOf(
                        height(grandParent.left),
                        height(grandParent.right)
                    ) + 1
                }
                if (insertionMode) {
                    return
                }
            }
            parent.height = maxOf(
                height(parent.left),
                height(parent.right)
            ) + 1
            parent = parent.parent
        }
    }

    fun isHealthy(): Boolean {
        return if (root == null) {
            true
        } else (!containsCycles()
                && heightsAreCorrect()
                && isBalanced(root)
                && isWellIndexed())
    }

    private fun containsCycles(): Boolean {
        val visitedNodes: MutableSet<Node<K, V>> = mutableSetOf()
        return containsCycles(root, visitedNodes)
    }

    private fun containsCycles(
        current: Node<K, V>?,
        visitedNodes: MutableSet<Node<K, V>>
    ): Boolean {
        if (current == null) {
            return false
        }
        if (visitedNodes.contains(current)) {
            return true
        }
        visitedNodes.add(current)
        return (containsCycles(current.left, visitedNodes)
                || containsCycles(current.right, visitedNodes))
    }

    private fun heightsAreCorrect(): Boolean {
        return getHeight(root) == root!!.height
    }

    private fun getHeight(node: Node<K, V>?): Int {
        if (node == null) {
            return -1
        }
        val leftTreeHeight = getHeight(node.left)
        if (leftTreeHeight == Int.MIN_VALUE) {
            return Int.MIN_VALUE
        }
        val rightTreeHeight = getHeight(node.right)
        if (rightTreeHeight == Int.MIN_VALUE) {
            return Int.MIN_VALUE
        }
        return if (node.height == maxOf(leftTreeHeight, rightTreeHeight) + 1) {
            node.height
        } else Int.MIN_VALUE
    }

    private fun isBalanced(node: Node<K, V>?): Boolean {
        if (node == null) {
            return true
        }
        if (!isBalanced(node.left)) {
            return false
        }
        if (!isBalanced(node.right)) {
            return false
        }
        val leftHeight = height(node.left)
        val rightHeight = height(node.right)
        return kotlin.math.abs(leftHeight - rightHeight) < 2
    }

    private fun isWellIndexed(): Boolean {
        return size == count(root)
    }

    private fun count(node: Node<K, V>?): Int {
        if (node == null) {
            return 0
        }
        val leftTreeSize = count(node.left)
        if (leftTreeSize == Int.MIN_VALUE) {
            return Int.MIN_VALUE
        }
        if (node.count != leftTreeSize) {
            return Int.MIN_VALUE
        }
        val rightTreeSize = count(node.right)
        return if (rightTreeSize == Int.MIN_VALUE) {
            Int.MIN_VALUE
        } else leftTreeSize + 1 + rightTreeSize
    }
}
