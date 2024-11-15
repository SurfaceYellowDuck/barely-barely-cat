class KDNode(
    val cat: Cat,
    val axis: Int,  // 0 для разделения по x, 1 для разделения по y
    var left: KDNode? = null,
    var right: KDNode? = null
)

class KDTree(cats: List<Cat>, distanceFunction: (Cat, Cat) -> Float) {
    var root: KDNode? = null
    var dist: (Cat, Cat) -> Float = Cat::distance

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

    // Поиск ближайшего соседа с исключением целевой точки
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
//        val currentDist = target.distance(node.cat)
        val currentDist = dist(target, node.cat)

        // Игнорируем точку, если она совпадает с целевой
        var newBestNode = bestNode
        var newBestDist = bestDist
        if (target != node.cat) {  // Добавляем проверку на совпадение
            if (currentDist < bestDist) {
                newBestNode = node
                newBestDist = currentDist
            }
        }

        // Определяем, в какое поддерево идти
        val diff = if (axis == 0) target.x - node.cat.x else target.y - node.cat.y
        val (nextNode, otherNode) = if (diff < 0) node.left to node.right else node.right to node.left

        // Рекурсивно ищем в ближайшем поддереве
        newBestNode = nearestNeighbor(nextNode, target, depth + 1, newBestNode, newBestDist)

        // Проверяем, нужно ли искать в другом поддереве
        if (Math.abs(diff) < newBestDist) {
            newBestNode = nearestNeighbor(
                otherNode,
                target,
                depth + 1,
                newBestNode,
//                newBestNode?.cat?.distance(target) ?: newBestDist
                if (newBestNode != null) dist(target, newBestNode.cat) else newBestDist
            )
        }

        return newBestNode
    }
}
