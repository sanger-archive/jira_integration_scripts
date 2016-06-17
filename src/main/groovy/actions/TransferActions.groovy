/**
 * See README.md for copyright details
 */
package actions

import com.sun.tools.javac.util.Pair
import exceptions.TransferException
import models.*

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
                     Map<String, List<Metadatum>> newMetadataToLocation = [:]) {

        if (sourceLabware.labwareType.layout != destinationLabware.labwareType.layout)
            throw new TransferException("Labwares must have the same layout. ${sourceLabware.labwareType.layout.name} and ${destinationLabware.labwareType.layout.name}")

        def transferMap = sourceLabware.receptacles.collect { new Pair<>(it.location.name, it.location.name) }
        destinationLabware = transfer(sourceLabware, destinationLabware, newMetadataToLocation, materialType, copyMetadata, transferMap)

        updateLabware(destinationLabware)
    }

    def static split(Labware sourceLabware, Labware destinationLabware, MaterialType materialType,
                     List<String> destinationLocations, List<String> copyMetadata = [],
                     Map<String, List<Metadatum>> newMetadataToLocation = [:]) {

        def destinationLabwareLocations =
            destinationLabware.receptacles.collect { it.location.name }
        def missingLocations = destinationLocations.findAll { !(it in destinationLabwareLocations) }
        if (missingLocations.size() > 0) {
            throw new TransferException("The following locations missing from the destination labware: ${missingLocations.join(', ')}")
        }

        def transferMap = destinationLocations.collect { new Pair<>(sourceLabware.receptacles[0].location.name, it) }
        destinationLabware = transfer(sourceLabware, destinationLabware, newMetadataToLocation, materialType, copyMetadata, transferMap)

        updateLabware(destinationLabware)
    }

    private static Labware transfer(Labware sourceLabware, Labware destinationLabware,
                                    Map<String, List<Metadatum>> newMetadataToLocation, MaterialType materialType,
                                    List<String> copyMetadata, List<Pair<String, String>> transferMap) {

        def receptacleMap = transferMap.collect { stringPair ->
            new Pair<Receptacle, Receptacle>(
                sourceLabware.receptacles.find { receptacle -> receptacle.location.name == stringPair.fst },
                destinationLabware.receptacles.find { receptacle -> receptacle.location.name == stringPair.snd }
            )
        }

        validateLocations(receptacleMap*.snd)

        def sourceMaterials = getMaterialsByUuid((receptacleMap*.fst.materialUuid).unique().findAll { it != null })
        def sourceMaterialsByUuid = sourceMaterials.collectEntries { [it.id, it] }
        def materialNameToLocationName = new HashMap<String, String>()

        def destinationMaterials = receptacleMap.collect { receptaclePair ->
            def locationName = receptaclePair.snd.location.name
            def newMetadataToAdd = newMetadataToLocation[locationName] ?: []
            def sourceMaterial = sourceMaterialsByUuid.get(receptaclePair.fst.materialUuid)
            if (sourceMaterial != null) {
                def childMaterial = createNewChildMaterial("${destinationLabware.barcode}_${locationName}",
                    materialType, sourceMaterial,
                    copyMetadata, newMetadataToAdd)
                materialNameToLocationName.put(childMaterial.name, locationName)
                childMaterial
            } else {
                null
            }
        }.findAll()
        destinationMaterials = postNewMaterials(destinationMaterials)

        destinationMaterials.each { material ->
            def locationName = materialNameToLocationName[material.name]
            def receptacle = destinationLabware.receptacles.find { it.location.name == locationName }
            receptacle.materialUuid = material.id
        }

        destinationLabware
    }

    private static getMaterialsByUuid(materialUuids) {
        MaterialActions.getMaterials(materialUuids)
    }

    private static createNewChildMaterial(materialName, type, sourceMaterial,
                                          copyMetadata, newMetadata) {
        new Material(
            name: materialName,
            materialType: type,
            metadata: sourceMaterial.metadata.findAll { it.key in copyMetadata } + newMetadata,
            parents: [sourceMaterial]
        )
    }

    private static postNewMaterials(destinationMaterials) {
        MaterialActions.postMaterials(destinationMaterials)
    }

    private static updateLabware(destinationLabware) {
        LabwareActions.updateLabware(destinationLabware)
    }

    private static validateLocations(receptacles) {
        def occupiedReceptacles = receptacles.findAll { it.materialUuid != null }

        if (occupiedReceptacles.size() > 0) {
            throw new TransferException(
                "The following locations already occupied in the destination labware: ${occupiedReceptacles*.location.name.join(', ')}")
        }
    }
}
