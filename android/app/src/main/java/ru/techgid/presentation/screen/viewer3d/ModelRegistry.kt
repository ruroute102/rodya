package ru.techgid.presentation.screen.viewer3d

import android.content.Context
import java.io.IOException

/**
 * Maps a car display name to a .glb asset under assets/models/.
 * Returns null if no model file exists — caller should fall back to the
 * procedural mesh renderer.
 *
 * Matching rules mirror buildMeshForCar(): brand substring match, case-insensitive.
 * To add a model, drop <key>.glb into android/app/src/main/assets/models/ —
 * see MODELS.md in the same folder for the full pipeline.
 */
object ModelRegistry {

    private val nameToAsset: List<Pair<(String) -> Boolean, String>> = listOf(
        { n: String -> n.contains("Audi Q3", ignoreCase = true) } to "audi_q3.glb",
        { n: String -> n.contains("BMW", ignoreCase = true) } to "bmw.glb",
        { n: String -> n.contains("Mercedes", ignoreCase = true) } to "mercedes.glb",
        { n: String -> n.contains("Toyota", ignoreCase = true) } to "toyota.glb",
        { n: String -> n.contains("Volkswagen", ignoreCase = true) ||
            n.contains("VW", ignoreCase = true) ||
            n.contains("Golf", ignoreCase = true) } to "volkswagen.glb",
        { n: String -> n.contains("Lada", ignoreCase = true) ||
            n.contains("\u0412\u0410\u0417", ignoreCase = true) } to "lada.glb",
        { n: String -> n.contains("Hyundai", ignoreCase = true) ||
            n.contains("Kia", ignoreCase = true) } to "hyundai.glb",
        { n: String -> n.contains("Skoda", ignoreCase = true) } to "skoda.glb",
        { n: String -> n.contains("Renault", ignoreCase = true) } to "renault.glb",
        { n: String -> n.contains("Mazda", ignoreCase = true) } to "mazda.glb",
        { n: String -> n.contains("Honda", ignoreCase = true) } to "honda.glb",
        { n: String -> n.contains("Nissan", ignoreCase = true) } to "nissan.glb",
        { n: String -> n.contains("Mitsubishi", ignoreCase = true) } to "mitsubishi.glb",
        { n: String -> n.contains("Ford", ignoreCase = true) } to "ford.glb",
        { n: String -> n.contains("Chevrolet", ignoreCase = true) } to "chevrolet.glb",
        { n: String -> n.contains("Lexus", ignoreCase = true) } to "lexus.glb",
        { n: String -> n.contains("Subaru", ignoreCase = true) } to "subaru.glb",
    )

    fun resolveAssetPath(carName: String, context: Context): String? {
        val candidate = nameToAsset.firstOrNull { (predicate, _) -> predicate(carName) }?.second
            ?: return null
        val fullPath = "models/$candidate"
        return if (assetExists(context, fullPath)) fullPath else null
    }

    private fun assetExists(context: Context, path: String): Boolean = try {
        context.assets.open(path).close()
        true
    } catch (e: IOException) {
        false
    }
}
