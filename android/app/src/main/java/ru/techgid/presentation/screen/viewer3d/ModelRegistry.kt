package ru.techgid.presentation.screen.viewer3d

import android.content.Context
import java.io.IOException

/**
 * Registry of production 3D assets.
 *
 * The target architecture is one GLB per real car configuration, not one generic
 * model per brand. The procedural renderer remains as a fallback until a GLB is
 * bundled for a specific configuration.
 */
object ModelRegistry {

    val assets: List<CarModelAsset> = listOf(
        CarModelAsset(
            carId = "audi_q3_8u_2011_2_0_tfsi",
            displayName = "Audi Q3 8U 2011 · 2.0 TFSI",
            modelAssetPath = "models/audi/q3/8u/2011_2_0_tfsi/model.glb",
            previewAssetPath = "models/audi/q3/8u/2011_2_0_tfsi/preview.png",
            metadataAssetPath = "models/audi/q3/8u/2011_2_0_tfsi/parts_metadata.json",
            cameraPresetsAssetPath = "models/audi/q3/8u/2011_2_0_tfsi/camera_presets.json",
            legacyAssetPath = "models/audi_q3.glb",
            supportsXray = true,
            matcher = { name ->
                name.contains("Audi Q3", ignoreCase = true) &&
                    name.contains("2.0", ignoreCase = true)
            },
            partGroups = listOf(
                PartMeshGroup(
                    partId = PartId.BODY,
                    displayName = "Полупрозрачный кузов",
                    meshNameHints = listOf("body", "body_shell", "doors", "fenders"),
                    materialRole = MaterialRole.XRAY_SHELL,
                ),
                PartMeshGroup(
                    partId = PartId.GLASS,
                    displayName = "Стёкла",
                    meshNameHints = listOf("glass", "windows", "windshield"),
                    materialRole = MaterialRole.GLASS,
                ),
                PartMeshGroup(
                    partId = PartId.REAR_SEAT,
                    displayName = "Заднее сиденье",
                    meshNameHints = listOf("rear_seat", "rear_seat_cushion", "seat_back_rear"),
                    materialRole = MaterialRole.INTERIOR,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_PUMP,
                    displayName = "Модуль топливного насоса",
                    meshNameHints = listOf("fuel_pump", "fuel_pump_module", "sea_pump"),
                    materialRole = MaterialRole.BLUE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_PUMP_COVER,
                    displayName = "Крышка доступа",
                    meshNameHints = listOf("fuel_access_cover", "fuel_pump_cover", "tank_cover"),
                    materialRole = MaterialRole.BLUE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_TANK,
                    displayName = "Топливный бак",
                    meshNameHints = listOf("fuel_tank", "tank"),
                    materialRole = MaterialRole.XRAY_INTERNAL,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_LOCKING_RING,
                    displayName = "Стопорное кольцо фланца",
                    meshNameHints = listOf("fuel_locking_ring", "locking_ring", "pump_lock_ring"),
                    materialRole = MaterialRole.XRAY_INTERNAL,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_CONNECTOR,
                    displayName = "Разъём модуля насоса",
                    meshNameHints = listOf("fuel_connector", "pump_connector", "flange_connector"),
                    materialRole = MaterialRole.ORANGE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_LINE,
                    displayName = "Топливная магистраль",
                    meshNameHints = listOf("fuel_line", "fuel_supply_line", "petrol_line"),
                    materialRole = MaterialRole.XRAY_INTERNAL,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_PUMP_CONTROLLER,
                    displayName = "Блок J538",
                    meshNameHints = listOf("fuel_pump_controller", "j538", "pump_control_unit"),
                    materialRole = MaterialRole.ORANGE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.FUEL_SENDER_LEFT,
                    displayName = "Левый датчик G169",
                    meshNameHints = listOf("fuel_sender_left", "fuel_gauge_sender_2", "g169"),
                    materialRole = MaterialRole.BLUE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.SUCTION_JET_PUMP,
                    displayName = "Suction-jet pump",
                    meshNameHints = listOf("suction_jet_pump", "jet_pump", "transfer_pump"),
                    materialRole = MaterialRole.BLUE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.ENGINE_BLOCK,
                    displayName = "Двигатель 2.0 TFSI",
                    meshNameHints = listOf("engine", "engine_block", "ea888"),
                    materialRole = MaterialRole.ORANGE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.OIL_FILTER,
                    displayName = "Масляный фильтр",
                    meshNameHints = listOf("oil_filter", "filter_oil"),
                    materialRole = MaterialRole.ORANGE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.TRANSMISSION,
                    displayName = "Коробка передач",
                    meshNameHints = listOf("transmission", "gearbox", "dsg", "dq250"),
                    materialRole = MaterialRole.ORANGE_HIGHLIGHT,
                ),
                PartMeshGroup(
                    partId = PartId.DRIVE_SHAFT,
                    displayName = "Приводные валы",
                    meshNameHints = listOf("drive_shaft", "cv_axle", "halfshaft"),
                    materialRole = MaterialRole.ORANGE_HIGHLIGHT,
                ),
            ),
        ),
    )

    fun resolveAssetSpec(carName: String, context: Context): ResolvedCarModelAsset? {
        val spec = assets.firstOrNull { it.matcher(carName) } ?: return null
        val bundledPath = when {
            assetExists(context, spec.modelAssetPath) -> spec.modelAssetPath
            spec.legacyAssetPath != null && assetExists(context, spec.legacyAssetPath) -> spec.legacyAssetPath
            else -> null
        }
        return ResolvedCarModelAsset(spec = spec, bundledModelAssetPath = bundledPath)
    }

    fun resolveAssetPath(carName: String, context: Context): String? {
        return resolveAssetSpec(carName, context)?.bundledModelAssetPath
    }

    private fun assetExists(context: Context, path: String): Boolean = try {
        context.assets.open(path).close()
        true
    } catch (e: IOException) {
        false
    }
}

data class ResolvedCarModelAsset(
    val spec: CarModelAsset,
    val bundledModelAssetPath: String?,
) {
    val hasBundledModel: Boolean get() = bundledModelAssetPath != null
}

data class CarModelAsset(
    val carId: String,
    val displayName: String,
    val modelAssetPath: String,
    val previewAssetPath: String,
    val metadataAssetPath: String,
    val cameraPresetsAssetPath: String,
    val supportsXray: Boolean,
    val partGroups: List<PartMeshGroup>,
    val legacyAssetPath: String? = null,
    val matcher: (String) -> Boolean,
)

data class PartMeshGroup(
    val partId: String,
    val displayName: String,
    val meshNameHints: List<String>,
    val materialRole: MaterialRole,
)

enum class MaterialRole {
    XRAY_SHELL,
    XRAY_INTERNAL,
    GLASS,
    INTERIOR,
    BLUE_HIGHLIGHT,
    ORANGE_HIGHLIGHT,
}
