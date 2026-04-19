package ru.techgid.presentation.screen.viewer3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

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

    var modelLoaded by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }

    val childNodes = rememberNodes {
        val result = runCatching {
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
        modelLoaded = result.isSuccess
        modelError = result.isFailure
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

        AnimatedVisibility(
            visible = !modelLoaded && !modelError,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF4A90D9),
                    strokeWidth = 3.dp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Загрузка модели…",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8899AA),
                )
            }
        }

        if (modelError) {
            Text(
                text = "Не удалось загрузить модель",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B6B),
            )
        }
    }
}
