package ru.techgid.presentation.screen.viewer3d

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

/**
 * Hardware-accelerated PBR renderer via Google Filament (SceneView wrapper).
 * Loads a .glb file from assets/ at [modelAssetPath].
 *
 * For loading rules and how to obtain .glb files, see
 * app/src/main/assets/models/MODELS.md.
 */
@Composable
fun Car3DGlbRenderer(
    modelAssetPath: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFF0A1020),
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0.6f, z = 4.2f)
        lookAt(Position(0f, 0f, 0f))
    }

    val childNodes = rememberNodes {
        runCatching {
            val instance = modelLoader.createModelInstance(assetFileLocation = modelAssetPath)
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 1.8f,
                centerOrigin = Position(y = -0.2f),
            ).apply {
                isEditable = true
            }
            add(node)
        }
    }

    Box(
        modifier = modifier.background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            cameraNode = cameraNode,
            childNodes = childNodes,
            isOpaque = false,
        )
    }
}
