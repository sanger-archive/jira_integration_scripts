/**
 * See README.md for copyright details
 */
package actions

import exceptions.TransferException

import models.Labware
import models.Material
import models.MaterialType
import models.Metadatum

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
        MaterialType materialType, List<String> copyMetadata = [],
        List<Metadatum> newMetadataToAdd = []) {

        if (sourceLabware.labwareType.layout != destinationLabware.labwareType.layout)
            throw new TransferException("Labwares must have the same layout. ${sourceLabware.labwareType.layout.name} and ${destinationLabware.labwareType.layout.name}")

        def sourceMaterialUuidToLocation = sourceLabware.receptacles.collectEntries { [it.materialUuid, it.location] }

        validateLocations(sourceMaterialUuidToLocation.values().collect { it.name }, destinationLabware)

        def sourceMaterials = getMaterialsByUuid(sourceLabware.materialUuids())
        def destinationMaterials = sourceMaterials.collect { sourceMaterial ->
            createNewChildMaterial("${destinationLabware.barcode}_${sourceMaterialUuidToLocation[sourceMaterial.id].name}", 
                materialType, sourceMaterial, copyMetadata, newMetadataToAdd)
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
        List<String> destinationLocations, List<String> copyMetadata = [],
        List<Metadatum> newMetadataToAdd = []) {

        def destinationLabwareLocations =
            destinationLabware.receptacles.collect { it.location.name }
        def missingLocations = destinationLocations.findAll { !(it in destinationLabwareLocations) }
        if (missingLocations.size() > 0) {
            throw new TransferException("The following locations missing from the destination labware: ${missingLocations.join(', ')}")
        }

        validateLocations(destinationLocations, destinationLabware)

        def sourceMaterial = getMaterialsByUuid(sourceLabware.materialUuids())[0]

        def destinationMaterials = new ArrayList<>()
        def materialsNameByDestinationLocation = new HashMap<String, String>()
        destinationLocations.each {
            destinationMaterials.add(
                createNewChildMaterial("${destinationLabware.barcode}_$it",
                    materialType, sourceMaterial, copyMetadata, newMetadataToAdd)
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

    private static getMaterialsByUuid(materialUuids) {
        MaterialActions.getMaterials(materialUuids)
    }

    private static createNewChildMaterial(materialName, type, sourceMaterial,
        copyMetadata, newMetadataToAdd) {
        new Material(
            name: materialName,
            materialType: type,
            metadata: (sourceMaterial.metadata.findAll { it.key in copyMetadata } << newMetadataToAdd).flatten(),
            parents: [sourceMaterial]
        )
    }

    private static postNewMaterials(destinationMaterials) {
        MaterialActions.postMaterials(destinationMaterials)
    }

    private static updateLabware(destinationLabware) {
        LabwareActions.updateLabware(destinationLabware)
    }

    private static validateLocations(locations, destinationLabware) {
        def occupiedLocations = locations.collect { location ->
            def receptacle = destinationLabware.receptacles.find {
                it.location.name == location && it.materialUuid != null
            }
            if (receptacle) {
                receptacle.location.name
            }
        }.findAll()

        if (occupiedLocations.size() > 0) {
            throw new TransferException("The following locations already occupied in the destination labware: ${occupiedLocations.join(', ')}")
        }
    }
}
