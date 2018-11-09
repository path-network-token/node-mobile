package network.path.mobilenode.ui.opengl.glutils

import android.content.Context
import network.path.mobilenode.ui.opengl.addMultiple
import network.path.mobilenode.ui.opengl.models.Model

class ObjLoader(
    context: Context,
    file: String,
    generateLines: Boolean = false,
    radius: Float = 1.0f,
    val color: FloatArray? = null,
    val useTexCoords: Boolean = true,
    val useNormals: Boolean = true
) {
    val numFaces: Int
    val combined: FloatArray
    val indices: ShortArray

    init {
        val loadedVertices = mutableListOf<Vertex>()
        val loadedTextures = mutableListOf<TextureCoord>()
        val loadedNormals = mutableListOf<Normal>()
        val faces = mutableListOf<Face>()

        context.assets.open(file).bufferedReader().use {
            while (true) {
                val line = it.readLine() ?: break
                val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                when (parts[0]) {
                    "v" -> loadedVertices.add(
                        Vertex(
                            parts[1].toFloat() * radius,
                            parts[2].toFloat() * radius,
                            parts[3].toFloat() * radius
                        )
                    )
                    "vt" -> loadedTextures.add(
                        TextureCoord(
                            parts[1].toFloat(),
                            1.0f - parts[2].toFloat()
                        )
                    )
                    "vn" -> loadedNormals.add(
                        Normal(
                            parts[1].toFloat() * radius,
                            parts[2].toFloat() * radius,
                            parts[3].toFloat() * radius
                        )
                    )
                    "f" -> faces.add(
                        Face(
                            FaceVertex.create(parts[1]),
                            FaceVertex.create(parts[2]),
                            FaceVertex.create(parts[3])
                        )
                    )
                }
            }
        }

        var elementsPerVertex = Model.COORDS_PER_VERTEX
        if (color != null) {
            elementsPerVertex += Model.COLORS_PER_VERTEX
        }
        if (useTexCoords) {
            elementsPerVertex += Model.TEXCOORDS_PER_VERTEX
        }
        if (useNormals) {
            elementsPerVertex += Model.NORMALS_PER_VERTEX
        }

        val coef = if (generateLines) 2 else 1
        numFaces = faces.size * 3 * coef

        combined = FloatArray(numFaces * elementsPerVertex)
        indices = ShortArray(numFaces * 3)

        var combinedIndex = 0
        var indexIndex = 0
        for (face in faces) {
            fun addFaceVertex(fv: FaceVertex) {
                val vertex = loadedVertices[fv.v]
                val textureCoord = loadedTextures[fv.t]
                val normal = loadedNormals[fv.n]

                // Coords
                combinedIndex = combined.addMultiple(
                    combinedIndex,
                    vertex.x, vertex.y, vertex.z
                )

                // Color
                if (color != null) {
                    combinedIndex = combined.addMultiple(
                        combinedIndex,
                        color.component1(),
                        color.component2(),
                        color.component3(),
                        color.component4()
                    )
                }

                if (useTexCoords) {
                    combinedIndex = combined.addMultiple(
                        combinedIndex,
                        textureCoord.u, textureCoord.v
                    )
                }
                if (useNormals) {
                    combinedIndex = combined.addMultiple(
                        combinedIndex,
                        normal.x, normal.y, normal.z
                    )
                }

                indices[indexIndex++] = (indexIndex - 1).toShort()
            }

            if (generateLines) {
                addFaceVertex(face.v1)
                addFaceVertex(face.v2)
                addFaceVertex(face.v2)
                addFaceVertex(face.v3)
                addFaceVertex(face.v3)
                addFaceVertex(face.v1)
            } else {
                addFaceVertex(face.v1)
                addFaceVertex(face.v2)
                addFaceVertex(face.v3)
            }
        }
    }

    private data class Vertex(val x: Float, val y: Float, val z: Float)
    private data class TextureCoord(val u: Float, val v: Float)
    private data class Normal(val x: Float, val y: Float, val z: Float)
    private data class FaceVertex(val v: Int, val t: Int, val n: Int) {
        companion object {
            fun create(s: String): FaceVertex {
                val indices = s.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return FaceVertex(
                    indices[0].toInt() - 1,
                    indices[1].toInt() - 1,
                    indices[2].toInt() - 1
                )
            }
        }
    }

    private data class Face(val v1: FaceVertex, val v2: FaceVertex, val v3: FaceVertex)
}