import kotlin.math.abs

class KDNode(
    val cat: Cat,
    val axis: Int,
    var left: KDNode? = null,
    var right: KDNode? = null
)

class KDTree(cats: List<Cat>, distanceFunction: (Cat, Cat) -> Float) {
    var root: KDNode? = null
    var dist: (Cat, Cat) -> Float

    init {
        root = buildTree(cats, 0)
        dist = distanceFunction
    }

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

    fun nearestNeighbor(target: Cat): Cat? {
        return nearestNeighbor(root, target, 0, null, Float.POSITIVE_INFINITY)?.cat
    }

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
