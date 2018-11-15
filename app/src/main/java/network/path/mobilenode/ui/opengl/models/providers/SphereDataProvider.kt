package network.path.mobilenode.ui.opengl.models.providers

import android.graphics.Color
import androidx.annotation.ColorInt
import network.path.mobilenode.ui.opengl.addMultiple
import network.path.mobilenode.ui.opengl.colorToFloatArray
import network.path.mobilenode.ui.opengl.models.Model
import java.util.*
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class SphereDataProvider(recursion: Int, radius: Float, @ColorInt color: Int = Color.WHITE) :
    Model.DataProvider {
    companion object {
        private val RNG = Random(12345678L)

        private const val MIN_ALPHA = 0.3f
        private const val MAX_ALPHA = 1f

        private const val MAX_OFFSET = Math.PI.toFloat() / 24f
    }

    private lateinit var indexArray: ShortArray
    private lateinit var vertexArray: FloatArray

    override val indices: ShortArray
        get() = indexArray

    override val vertices: FloatArray
        get() = vertexArray

    override val hasColor = true
    override val hasTexture = false
    override val hasNormals = true

    private val vertexObjects = mutableListOf<Vertex>()
    private val cache = mutableMapOf<Long, Int>()

    init {
        generate(recursion, radius, colorToFloatArray(color))
    }

    private fun key(v1: Int, v2: Int, adjust: Boolean = false): Long {
        val first = if (adjust) min(v1, v2) else v1
        val second = if (adjust) max(v1, v2) else v2
        return first.toLong().shl(32) + second
    }

    private fun generate(recursion: Int, radius: Float, color: FloatArray) {
        vertexObjects.clear()

        val t = ((1.0 + Math.sqrt(5.0)) / 2.0).toFloat()
        addVertex(Vertex(-1f, t, 0f))
        addVertex(Vertex(1f, t, 0f))
        addVertex(Vertex(-1f, -t, 0f))
        addVertex(Vertex(1f, -t, 0f))

        addVertex(Vertex(0f, -1f, t))
        addVertex(Vertex(0f, 1f, t))
        addVertex(Vertex(0f, -1f, -t))
        addVertex(Vertex(0f, 1f, -t))

        addVertex(Vertex(t, 0f, -1f))
        addVertex(Vertex(t, 0f, 1f))
        addVertex(Vertex(-t, 0f, -1f))
        addVertex(Vertex(-t, 0f, 1f))

        val faces = mutableListOf<Face>()

        faces.add(Face(0, 11, 5))
        faces.add(Face(0, 5, 1))
        faces.add(Face(0, 1, 7))
        faces.add(Face(0, 7, 10))
        faces.add(Face(0, 10, 11))

        faces.add(Face(10, 2, 11))
        faces.add(Face(4, 5, 11))
        faces.add(Face(9, 1, 5))
        faces.add(Face(8, 7, 1))
        faces.add(Face(6, 10, 7))

        faces.add(Face(2, 4, 11))
        faces.add(Face(4, 9, 5))
        faces.add(Face(9, 8, 1))
        faces.add(Face(8, 6, 7))
        faces.add(Face(6, 2, 10))

        faces.add(Face(2, 6, 3))
        faces.add(Face(6, 8, 3))
        faces.add(Face(8, 9, 3))
        faces.add(Face(9, 4, 3))
        faces.add(Face(4, 2, 3))

        for (rIndex in 0 until recursion) {
            val newFaces = mutableListOf<Face>()
            faces.forEach {
                val a = getMiddlePoint(it.v1, it.v2)
                val b = getMiddlePoint(it.v2, it.v3)
                val c = getMiddlePoint(it.v3, it.v1)

                newFaces.add(Face(it.v1, a, c))
                newFaces.add(Face(it.v2, b, a))
                newFaces.add(Face(it.v3, c, b))
                newFaces.add(Face(a, b, c))
            }
            faces.clear()
            faces.addAll(newFaces)
        }

        fun MutableMap<Long, MutableList<Face>>.addAdjacentFace(f: Face, key: Long) {
            if (this.containsKey(key)) {
                this[key]?.add(f)
            } else {
                this[key] = mutableListOf(f)
            }
        }

        val adjacentFaces = faces.fold(mutableMapOf<Long, MutableList<Face>>()) { map, f ->
            map.addAdjacentFace(f, key(f.v1, f.v2, true))
            map.addAdjacentFace(f, key(f.v2, f.v3, true))
            map.addAdjacentFace(f, key(f.v3, f.v1, true))
            map
        }.filterValues { it.size > 1 }

        fun MutableMap<Long, LineIndices>.addLine(v1: Int, v2: Int): Boolean {
            if (v1 == v2) return false
            val key = key(v1, v2)
            if (!contains(key)) {
                this[key] = LineIndices(v1, v2)
                return true
            }
            return false
        }

        fun Map<Long, MutableList<Face>>.addUncommonLine(
            v1: Int,
            v2: Int,
            lines: MutableMap<Long, LineIndices>
        ): Boolean {
            val foundFaces = this[key(v1, v2, true)] ?: return false
            val line = foundFaces.first().findUncommonLine(foundFaces.last()) ?: return false
            return lines.addLine(line.v1, line.v2)
        }

        val lines = faces.fold(mutableMapOf<Long, LineIndices>()) { map, face ->
            map.addLine(face.v1, face.v2)
            map.addLine(face.v2, face.v3)
            map.addLine(face.v3, face.v1)
            if (RNG.nextFloat() > 0.75f) {
                if (!adjacentFaces.addUncommonLine(face.v1, face.v2, map))
                    if (!adjacentFaces.addUncommonLine(face.v2, face.v3, map))
                        adjacentFaces.addUncommonLine(face.v3, face.v1, map)
            }
            map
        }.values

        fun FloatArray.addVertex(i: Int, v: Vertex, color: FloatArray, alphaMultiplier: Float = 1.0f, normal: Vertex): Int =
            this.addMultiple(
                i,
                v.x * radius,
                v.y * radius,
                v.z * radius,
                color.component1(),
                color.component2(),
                color.component3(),
                color.component4() * alphaMultiplier,
                normal.x,
                normal.y,
                normal.z
            )

        val movedVertices = vertexObjects.map { move(it) }
        var index = 0
        val coloredVertices = FloatArray(lines.size * 2 * (Model.COORDS_PER_VERTEX + Model.COLORS_PER_VERTEX + Model.NORMALS_PER_VERTEX))
        lines.fold(coloredVertices) { array, line ->
            val v1 = movedVertices[line.v1]
            val v2 = movedVertices[line.v2]
            val dx = v1.x - v2.x
            val dy = v1.y - v2.y
            val dz = v1.z - v2.z
            val d = Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
            val alphaMultiplier = max(min((0.7f - d) / (0.7f - 0.3f),
                MAX_ALPHA
            ), MIN_ALPHA
            )
            index = array.addVertex(index, v1, color, alphaMultiplier, v1)
            index = array.addVertex(index, v2, color, alphaMultiplier, v2)
            array
        }

//            var vIndex = 0
//            val vertices = FloatArray(vertexObjects.size * (Model.COORDS_PER_VERTEX + Model.COLORS_PER_VERTEX))
//            vertexObjects.forEach {
//                val v = move(it)
//                vIndex = vertices.addVertex(vIndex, v, color)
//            }

//            var iIndex = 0
//            val indices = ShortArray(lines.size * 2)
//            lines.forEach { iIndex = indices.addMultiple(iIndex, it.v1.toShort(), it.v2.toShort()) }
//            lines.forEach { iIndex = indices.addMultiple(iIndex, iIndex.toShort(), (iIndex + 1).toShort()) }
//            indexArray = indices
        indexArray = (0 until lines.size * 2).map { it.toShort() }.toShortArray()

//            vertexArray = vertices.copyOf()
        vertexArray = coloredVertices.copyOf()
    }

    private fun move(v: Vertex): Vertex {
        val r = v.length
        var theta = acos(v.z / r)
        var phi = atan2(v.y, v.x)

        val offsetTheta = RNG.nextFloat() * MAX_OFFSET
        if (theta > offsetTheta) {
            theta -= offsetTheta
        } else {
            theta = offsetTheta - theta
            phi = -phi
        }
        val offsetPhi = RNG.nextFloat() * MAX_OFFSET
        phi -= offsetPhi
        if (phi < 0) {
            phi += 2f * Math.PI.toFloat()
        }
        return Vertex(
            r * sin(theta) * cos(phi), r * sin(theta) * sin(
                phi
            ), r * cos(theta)
        )
    }

    private fun addVertex(v: Vertex): Int {
        val length = v.length
        vertexObjects.add(
            Vertex(
                v.x / length,
                v.y / length,
                v.z / length
            )
        )
        return vertexObjects.lastIndex
    }

    private fun getMiddlePoint(p1: Int, p2: Int): Int {
        val key = key(p1, p2, true)

        val value = cache[key]
        if (value != null) return value

        val point1 = vertexObjects[p1]
        val point2 = vertexObjects[p2]
        val middle = Vertex(
            (point1.x + point2.x) / 2,
            (point1.y + point2.y) / 2,
            (point1.z + point2.z) / 2
        )

        return addVertex(middle).also { cache[key] = it }
    }

    private data class Vertex(val x: Float, val y: Float, val z: Float) {
        val length = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    private data class Face(val v1: Int, val v2: Int, val v3: Int) {
        fun findUncommonLine(f: Face): LineIndices? {
            val s1 = setOf(f.v1, f.v2, f.v3)
            val s2 = setOf(v1, v2, v3)
            val s = s1.union(s2).minus(s1.intersect(s2))
            return if (s.size == 2) {
                LineIndices(s.first(), s.last())
            } else null
        }
    }

    private data class LineIndices(val v1: Int, val v2: Int)
}