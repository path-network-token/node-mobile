package network.path.mobilenode.ui.opengl.models.providers

import network.path.mobilenode.ui.opengl.models.Model
import network.path.mobilenode.ui.opengl.glutils.ObjLoader

class ObjDataProvider(private val loader: ObjLoader) :
    Model.DataProvider {
    override val vertices: FloatArray
        get() = loader.combined

    override val indices: ShortArray
        get() = loader.indices

    override val hasColor = loader.color != null
    override val hasTexture = loader.useTexCoords
    override val hasNormals = loader.useNormals
}