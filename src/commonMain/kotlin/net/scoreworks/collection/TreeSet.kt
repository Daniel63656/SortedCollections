package net.scoreworks.collection


open class TreeSet<E : Comparable<E>> : MutableSortedSet<E> {
    private var root: Node<E>? = null
    private var _size: Int = 0  // Internal size variable
    private var modCount = 0    // For concurrency
    override val size: Int
        get() = _size

    private class Node<E>(var key: E) {
        var parent: Node<E>? = null
        var left: Node<E>? = null
        var right: Node<E>? = null
        var height = 0
        var count = 0
    }

    override fun clear() {
        modCount += _size
        root = null
        _size = 0
    }

    override fun isEmpty(): Boolean {
        return _size == 0
    }

    override fun contains(element: E): Boolean {
        return getNode(element) != null
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements) {
            if (!contains(element)) {
                return false
            }
        }
        return true
    }

    override operator fun get(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("The input index is out of bounds.")
        }
        var idx = index
        // Now root should exist!
        var node: Node<E> = root!!

        while (true) {
            if (index > node.count) {
                idx -= node.count + 1
                node = node.right!!
            } else if (index < node.count) {
                node = node.left!!
            } else {
                return node.key
            }
        }
    }

    override fun indexOf(element: E): Int {
        var node: Node<E>? = root
        if (root == null) {
            return -1
        }
        var rank = root!!.count
        var cmp: Int
        while (node != null) {
            if (element.compareTo(node.key).also { cmp = it } < 0) {
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

    override fun iterator(): MutableIterator<E> {
        return TreeIterator()
    }

    override fun add(element: E): Boolean {
        if (root == null) {
            root = Node(element)
            _size = 1
            modCount++
            return true
        }
        var parent: Node<E>? = null
        var node = root
        var cmp: Int
        while (node != null) {
            cmp = element.compareTo(node.key)
            if (cmp == 0) {
                // The element is already in this tree.
                return false
            }
            parent = node
            node = if (cmp < 0) node.left else node.right
        }
        val newNode = Node(element)
        if (element < parent!!.key) parent.left = newNode else parent.right = newNode
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
        return true
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var modified = false
        for (element in elements) {
            if (add(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun remove(element: E): Boolean {
        var x = getNode(element) ?: return false
        x = deleteNode(x)
        fixAfterModification(x, false)
        _size--
        modCount++
        return true
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var modified = false
        for (element in elements) {
            if (remove(element)) {
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        // Create a HashSet from the elements if it is not already a HashSet
        val elementSet = if (elements is HashSet<*>) elements as HashSet<E> else HashSet(elements)
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

    override fun higher(element: E): E? {
        return higherNode(element)?.key
    }

    override fun lower(element: E): E? {
        return lowerNode(element)?.key
    }

    override fun ceiling(element: E): E? {
        if (contains(element)) return element
        return higherNode(element)?.key
    }

    override fun floor(element: E): E? {
        if (contains(element)) return element
        return lowerNode(element)?.key
    }

    override fun first(): E? {
        return root?.let { minimumNode(it).key }
    }

    override fun last(): E? {
        return root?.let { maximumNode(it).key }
    }


    override fun rangedQuery(start: E?, end: E?, startInclusive: Boolean, endInclusive: Boolean, reverse: Boolean): Iterator<E> {
        if (root == null) {
            // Is empty; return an empty iterator
            return emptyList<E>().iterator()
        }
        if (!reverse) {
            // Determine the start node for iteration
            val startNode: Node<E>? = if (start != null) {
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
            val stop: E = end ?: maximumNode(root!!).key

            return object : Iterator<E> {
                private var nextNode: Node<E>? = startNode
                private var expectedModCount: Int = modCount    // detect concurrent modifications

                override fun hasNext(): Boolean {
                    return nextNode != null && ((endInclusive && nextNode!!.key <= stop) || (!endInclusive && nextNode!!.key < stop))
                }

                override fun next(): E {
                    val currentNode = nextNode ?: throw NoSuchElementException("Iteration exceeded.")
                    if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                    nextNode = successor(nextNode!!)
                    return currentNode.key
                }
            }
        }
        else {
            // Determine the start node for reverse iteration
            val startNode: Node<E>? = if (end != null) {
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
            val stop: E = start ?: minimumNode(root!!).key

            return object : Iterator<E> {
                private var nextNode: Node<E>? = startNode
                private var expectedModCount: Int = modCount // detect concurrent modifications

                override fun hasNext(): Boolean {
                    return nextNode != null && ((startInclusive && nextNode!!.key >= stop) || (!startInclusive && nextNode!!.key > stop))
                }

                override fun next(): E {
                    val currentNode = nextNode ?: throw NoSuchElementException("Iteration exceeded.")
                    if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
                    nextNode = predecessor(currentNode)
                    return currentNode.key
                }
            }
        }
    }

    private inner class TreeIterator : MutableIterator<E> {
        private var previousNode: Node<E>? = null
        private var nextNode: Node<E>? = null
        private var expectedModCount: Int = modCount    // detect concurrent modifications

        init {
            nextNode = root?.let { minimumNode(it) }
        }

        override fun hasNext(): Boolean {
            return nextNode != null
        }

        override fun next(): E {
            val currentNode = nextNode ?: throw NoSuchElementException("Iteration exceeded.")
            if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
            previousNode = nextNode
            nextNode = successor(currentNode)
            return currentNode.key
        }

        override fun remove() {
            checkNotNull(previousNode) { if (nextNode == null) "Not a single call to next(); nothing to remove." else "Removing the same element twice." }
            if (expectedModCount != modCount) throw ConcurrentModificationException("The set was modified while iterating.")
            val x: Node<E> = deleteNode(previousNode!!)
            fixAfterModification(x, false)
            if (x == nextNode) {
                nextNode = previousNode
            }
            expectedModCount = ++modCount
            _size--
            previousNode = null
        }
    }

    // ==================== internal functions ==================== //

    private fun getNode(element: E): Node<E>? {
        var x = root
        var cmp = 0
        while (x != null && element.compareTo(x.key).also { cmp = it } != 0) {
            x = if (cmp < 0) x.left else x.right
        }
        return x
    }

    private fun successor(node: Node<E>): Node<E>? {
        var n: Node<E> = node
        if (n.right != null) {
            n = n.right!!
            while (n.left != null) {
                n = n.left!!
            }
            return n
        }
        var parent: Node<E>? = n.parent
        while (parent != null && parent.right == n) {
            n = parent
            parent = parent.parent
        }
        return parent
    }

    private fun predecessor(node: Node<E>): Node<E>? {
        var n: Node<E> = node
        if (n.left != null) {
            n = n.left!!
            while (n.right != null) {
                n = n.right!!
            }
            return n
        }
        var parent: Node<E>? = n.parent
        while (parent != null && parent.left == n) {
            n = parent
            parent = parent.parent
        }
        return parent
    }

    private fun higherNode(element: E): Node<E>? {
        var node = root
        var successor: Node<E>? = null
        while (node != null) {
            if (element < node.key) {
                successor = node // Current node is a candidate
                node = node.left
            } else {
                node = node.right
            }
        }
        return successor
    }

    private fun lowerNode(element: E): Node<E>? {
        var node = root
        var candidate: Node<E>? = null
        while (node != null) {
            if (element <= node.key) {
                node = node.left
            } else {
                candidate = node // Current node is a candidate
                node = node.right
            }
        }
        return candidate
    }

    private fun minimumNode(node: Node<E>): Node<E> {
        var n: Node<E> = node
        while (n.left != null) {
            n = n.left!!
        }
        return n
    }

    private fun maximumNode(node: Node<E>): Node<E> {
        var n: Node<E> = node
        while (n.right != null) {
            n = n.right!!
        }
        return n
    }

    private fun deleteNode(node: Node<E>): Node<E> {
        if (node.left == null && node.right == null) {
            // 'node' has no children.
            val parent: Node<E>? = node.parent
            if (parent == null) {
                // 'node' is the root node of this tree.
                root = null
                ++modCount
                return node
            }
            var lo: Node<E>? = node
            var hi: Node<E>? = parent
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
            val tmpKey: E = node.key
            val successor: Node<E> = minimumNode(node.right!!)
            node.key = successor.key
            val child: Node<E>? = successor.right
            val parent: Node<E> = successor.parent!!
            if (parent.left == successor) {
                parent.left = child
            } else {
                parent.right = child
            }
            if (child != null) {
                child.parent = parent
            }
            var lo: Node<E>? = child
            var hi: Node<E>? = parent
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
        val child: Node<E> = if (node.left != null) node.left!! else node.right!!
        val parent: Node<E>? = node.parent
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
        var hi: Node<E>? = parent
        var lo: Node<E> = child
        while (hi != null) {
            if (hi.left == lo) {
                hi.count--
            }
            lo = hi
            hi = hi.parent
        }
        return node
    }

    private fun height(node: Node<E>?): Int {
        return node?.height ?: -1
    }

    private fun leftRotate(node1: Node<E>): Node<E> {
        val node2: Node<E> = node1.right!!
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

    private fun rightRotate(node1: Node<E>): Node<E> {
        val node2: Node<E> = node1.left!!
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

    private fun rightLeftRotate(node1: Node<E>): Node<E> {
        val node2: Node<E> = node1.right!!
        node1.right = rightRotate(node2)
        return leftRotate(node1)
    }

    private fun leftRightRotate(node1: Node<E>): Node<E> {
        val node2: Node<E> = node1.left!!
        node1.left = leftRotate(node2)
        return rightRotate(node1)
    }

    private fun fixAfterModification(node: Node<E>, insertionMode: Boolean) {
        // If insertionMode is false, this fixes a deletion instead
        var parent: Node<E>? = node.parent
        var grandParent: Node<E>?
        var subTree: Node<E>
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
        val visitedNodes: MutableSet<Node<E>> = mutableSetOf()
        return containsCycles(root, visitedNodes)
    }

    private fun containsCycles(
        current: Node<E>?,
        visitedNodes: MutableSet<Node<E>>
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

    private fun getHeight(node: Node<E>?): Int {
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

    private fun isBalanced(node: Node<E>?): Boolean {
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

    private fun count(node: Node<E>?): Int {
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
