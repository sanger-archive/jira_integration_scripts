/**
 * See README.md for copyright details
 */
package actions

import exceptions.TransferException

import models.Labware
import models.Material
import models.MaterialType

import actions.MaterialActions
import actions.LabwareActions

/**
 * The {@code TransferActions} class represents a class for transfer related actions.
 *
 * @author rf9
 * @author ke4
 *
 */
class TransferActions {

    def static stamp(Labware sourceLabware, Labware destinationLabware, 
        MaterialType materialType, List<String> copyMetadata = []) {

        if (sourceLabware.labwareType.layout != destinationLabware.labwareType.layout)
            throw new TransferException("Labwares must have the same layout. ${sourceLabware.labwareType.layout.name} and ${destinationLabware.labwareType.layout.name}")

        def sourceMaterialUuidToLocation = sourceLabware.receptacles.collectEntries { [it.materialUuid, it.location] }

        def sourceMaterials = getMaterialsFromLabware(sourceLabware.materialUuids())
        def destinationMaterials = sourceMaterials.collect { sourceMaterial ->
            createNewChildMaterial("${destinationLabware.barcode}_${sourceMaterialUuidToLocation[sourceMaterial.id].name}", 
                materialType, sourceMaterial, copyMetadata)
        }
        destinationMaterials = postNewMaterials(destinationMaterials)

        destinationMaterials.each { material ->
            def location = sourceMaterialUuidToLocation[material.parents[0].id]
            def receptacle = destinationLabware.receptacles.find { it.location == location }
            receptacle.materialUuid = material.id
        }

        updateLabware(destinationLabware)
    }

    def static split(Labware sourceLabware, Labware destinationLabware, MaterialType materialType,
        List<String> destinationLocations, List<String> copyMetadata = []) {

        def destinationLabwareLocations =
            destinationLabware.receptacles.collect { it.location.name }
        def missingLocations = new ArrayList<String>()
        destinationLocations.each { destinationLocation ->
            if (!destinationLabwareLocations.contains(destinationLocation)) {
                missingLocations.add(destinationLocation)
            }
        }
        if (missingLocations.size > 0) {
            throw new TransferException("The following locations missing from the destination labware: ${missingLocations.join(', ')}")
        }

        def sourceMaterial = getMaterialsFromLabware(sourceLabware.materialUuids())

        def destinationMaterials = new ArrayList<>()
        def materialsNameByDestinationLocation = new HashMap<String, String>()
        destinationLocations.each {
            destinationMaterials.add(
                createNewChildMaterial("${destinationLabware.barcode}_$it",
                    materialType, sourceMaterial, copyMetadata)
            )
            materialsNameByDestinationLocation.put(it, "${destinationLabware.barcode}_$it")
        }
        destinationMaterials = postNewMaterials(destinationMaterials)

        materialsNameByDestinationLocation.each { location, materialName ->
            def material = destinationMaterials.find { it.name == materialName }
            def receptacle = destinationLabware.receptacles.find { it.location.name == location }
            receptacle.materialUuid = material.id
        }

        updateLabware(destinationLabware)
    }

    private static getMaterialsFromLabware(materialUuids) {
        MaterialActions.getMaterials(materialUuids)
    }

    private static createNewChildMaterial(materialName, type, sourceMaterial, copyMetadata) {
        new Material(
            name: materialName,
            materialType: type,
            metadata: sourceMaterial.metadata.findAll { it.key in copyMetadata },
            parents: [sourceMaterial]
        )
    }

    private static postNewMaterials(destinationMaterials) {
        MaterialActions.postMaterials(destinationMaterials)
    }

    private static updateLabware(destinationLabware) {
        LabwareActions.updateLabware(destinationLabware)
    }
}
