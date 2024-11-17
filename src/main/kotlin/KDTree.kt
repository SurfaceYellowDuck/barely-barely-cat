import kotlin.math.abs

/**
 * Represents a node in a K-D tree for organizing spatial data.
 *
 * @property cat The cat object containing spatial coordinates.
 * @property axis The axis (dimension) used to partition the space at this node (0 for x-axis, 1 for y-axis).
 * @property left The left child node in the K-D tree.
 * @property right The right child node in the K-D tree.
 */
class KDNode(
    val cat: Cat,
    val axis: Int,
    var left: KDNode? = null,
    var right: KDNode? = null
)

/**
 * A K-D Tree implementation specialized for managing `Cat` objects in two-dimensional space.
 *
 * This class allows for efficient nearest neighbor searches by organizing spatial data into a binary tree where each split alternates between the x and y axes.
 *
 * @param cats The initial list of `Cat` objects to be organized in the K-D tree.
 * @param distanceFunction A function that computes the distance between two `Cat` objects.
 */
class KDTree(cats: List<Cat>, distanceFunction: (Cat, Cat) -> Float) {
    var root: KDNode? = null
    var dist: (Cat, Cat) -> Float

    init {
        root = buildTree(cats, 0)
        dist = distanceFunction
    }

    /**
     * Builds a K-D tree from a list of Cat objects.
     *
     * @param points The list of Cat objects to be inserted into the K-D tree.
     * @param depth The current depth in the K-D tree, used to determine the splitting axis.
     * @return The root node of the constructed K-D tree, or null if the input list is empty.
     */
    private fun buildTree(points: List<Cat>, depth: Int): KDNode? {
        if (points.isEmpty()) return null
        val axis = depth % 2
        val sortedPoints = points.sortedWith(compareBy { if (axis == 0) it.x else it.y })
        val medianIndex = sortedPoints.size / 2
        val medianPoint = sortedPoints[medianIndex]
        return KDNode(
            cat = medianPoint,
            axis = axis,
            left = buildTree(sortedPoints.subList(0, medianIndex), depth + 1),
            right = buildTree(sortedPoints.subList(medianIndex + 1, sortedPoints.size), depth + 1)
        )
    }

    /**
     * Finds the nearest neighbor of a target cat in the KD-Tree.
     *
     * @param target The target cat for which the nearest neighbor is being searched.
     * @return The nearest neighbor cat to the target cat, or null if the KD-Tree is empty.
     */
    fun nearestNeighbor(target: Cat): Cat? {
        return nearestNeighbor(root, target, 0, null, Float.POSITIVE_INFINITY)?.cat
    }

    /**
     * Finds the nearest neighbor of a target cat in a KD-Tree.
     * This method is a recursive helper function for the public `nearestNeighbor` method.
     *
     * @param node The current KD-Tree node being examined.
     * @param target The target cat for which the nearest neighbor is being searched.
     * @param depth The current depth in the KD-Tree.
     * @param bestNode The current best node representing the nearest neighbor found so far.
     * @param bestDist The distance to the current best node.
     * @return The KDNode representing the nearest neighbor of the target cat.
     */
    private fun nearestNeighbor(
        node: KDNode?,
        target: Cat,
        depth: Int,
        bestNode: KDNode?,
        bestDist: Float
    ): KDNode? {
        if (node == null) return bestNode

        val axis = node.axis
        val currentDist = dist(target, node.cat)

        var newBestNode = bestNode
        var newBestDist = bestDist
        if (target != node.cat) {
            if (currentDist < bestDist) {
                newBestNode = node
                newBestDist = currentDist
            }
        }

        val diff = if (axis == 0) target.x - node.cat.x else target.y - node.cat.y
        val (nextNode, otherNode) = if (diff < 0) node.left to node.right else node.right to node.left

        newBestNode = nearestNeighbor(nextNode, target, depth + 1, newBestNode, newBestDist)

        if (abs(diff) < newBestDist) {
            newBestNode = nearestNeighbor(
                otherNode,
                target,
                depth + 1,
                newBestNode,
                if (newBestNode != null) dist(target, newBestNode.cat) else newBestDist
            )
        }

        return newBestNode
    }
}
