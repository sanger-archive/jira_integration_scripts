/**
 * See README.md for copyright details
 */
package actions

import java.util.List
import java.util.ArrayList
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
            throw new TransferException(
                "Labwares must have the same layout. ${sourceLabware.labwareType.layout.name} and ${destinationLabware.labwareType.layout.name}")

        def transferMap = sourceLabware.receptacles.collect {
            new ArrayList<?>(Arrays.asList(it.location.name, it.location.name))
        }
        destinationLabware = transfer(sourceLabware, destinationLabware,
            newMetadataToLocation, materialType, copyMetadata, transferMap)

        updateLabware(destinationLabware)
    }

    def static split(Labware sourceLabware, Labware destinationLabware,
                    MaterialType materialType, List<String> destinationLocations,
                    List<String> copyMetadata = [],
                    Map<String, List<Metadatum>> newMetadataToLocation = [:]) {

        def destinationLabwareLocations =
            destinationLabware.receptacles.collect { it.location.name }
        def missingLocations = destinationLocations.findAll {
            !(it in destinationLabwareLocations)
        }
        if (missingLocations.size() > 0) {
            throw new TransferException(
                "The following locations missing from the destination labware: ${missingLocations.join(', ')}")
        }

        def transferMap = destinationLocations.collect {
            new ArrayList<?>(Arrays.asList(
                sourceLabware.receptacles[0].location.name, it))
        }
        destinationLabware = transfer(sourceLabware, destinationLabware,
            newMetadataToLocation, materialType, copyMetadata, transferMap)

        updateLabware(destinationLabware)
    }

    def static combine(List<Labware> sourceLabwares, Labware destinationLabware,
                        MaterialType materialType, List<String> copyMetadata = [],
                        Map<String, List<Metadatum>> newMetadataToLocation = [:]) {

        validateCombineParameters(sourceLabwares, destinationLabware)

        (0..sourceLabwares.size()-1).each { plateNumber ->
            def sourceLabware = sourceLabwares.getAt(plateNumber)
            def transferMap = createTransferMapForCombine(sourceLabwares.size(),
                sourceLabware.labwareType.layout, plateNumber)

            destinationLabware = transfer(sourceLabware, destinationLabware,
                newMetadataToLocation, materialType, copyMetadata, transferMap)
        }

        updateLabware(destinationLabware)
    }

    private static Labware transfer(Labware sourceLabware, Labware destinationLabware,
                                    Map<String, List<Metadatum>> newMetadataToLocation,
                                    MaterialType materialType,
                                    List<String> copyMetadata,
                                    List<List<String>> transferMap) {

        def receptacleMap = transferMap.collect { stringPair ->
            new ArrayList<Receptacle>(
                Arrays.asList(
                    sourceLabware.receptacles.find {
                        receptacle -> receptacle.location.name == stringPair.getAt(0)
                    },
                    destinationLabware.receptacles.find {
                        receptacle -> receptacle.location.name == stringPair.getAt(1)
                    }
                    )
            )
        }

        validateLocations(receptacleMap*.getAt(1))

        def sourceMaterials = getMaterialsByUuid(
            (receptacleMap*.getAt(0).materialUuid).unique().findAll {
                it != null
            })
        def sourceMaterialsByUuid = sourceMaterials.collectEntries { [it.id, it] }
        def materialNameToLocationName = new HashMap<String, String>()

        def destinationMaterials = receptacleMap.collect { receptaclePair ->
            def locationName = receptaclePair.getAt(1).location.name
            def newMetadataToAdd = newMetadataToLocation[locationName] ?: []
            def sourceMaterial = sourceMaterialsByUuid.get(receptaclePair.getAt(0).materialUuid)
            if (sourceMaterial != null) {
                def childMaterial =
                    createNewChildMaterial("${destinationLabware.barcode}_${locationName}",
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
            def receptacle = destinationLabware.receptacles.find {
                it.location.name == locationName
            }
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
            metadata: sourceMaterial.metadata.findAll {
                it.key in copyMetadata
            } + newMetadata,
            parents: [sourceMaterial]
        )
    }

    private static postNewMaterials(destinationMaterials) {
        MaterialActions.postMaterials(destinationMaterials)
    }

    private static updateLabware(destinationLabware) {
        LabwareActions.updateLabware(destinationLabware)
    }

    private static createTransferMapForCombine(numberOfSourcePlates,
                                            sourceLayout, plateNumber) {
        def numberOfRows = sourceLayout.row
        def numberOfColumns = sourceLayout.column

        def transferMap = [];
        (1..numberOfRows).each { row ->
            def transferMapRow =
                (1..numberOfColumns).collect { column ->
                    def sourceRow = (char) (64 + row)
                    def sourceColumn = column

                    def destinationRow = 64 + (row * 2)
                    if ((int)(plateNumber / 2) == 0) {
                        destinationRow--
                    }
                    destinationRow = (char) destinationRow

                    def destinationColumn = column * 2
                    if (plateNumber % 2 == 0) {
                        destinationColumn--
                    }

                    new ArrayList<?>(Arrays.asList(
                            "$sourceRow$sourceColumn",
                            "$destinationRow$destinationColumn"
                        )
                    )
                }
            transferMap.addAll(transferMapRow)
        }

        transferMap
    }

    private static validateCombineParameters(sourceLabwares, destinationLabware) {
        if (!sourceLabwares) {
            throw new TransferException("Must supply at least one source plate")
        }

        if (sourceLabwares.size() > 4) {
            throw new TransferException("Must supply at most four source plates")
        }

        def layouts = sourceLabwares.collect { labware -> labware.labwareType.layout }.unique()
        if (layouts.size() > 1) {
            throw new TransferException("All source plates must have the same layout")
        }

        def sourceReceptaclesCount = sourceLabwares.inject(0) {
            result, labware -> result + labware.labwareType.layout.locations.size()
        }
        def destinationReceptaclesCount =
            destinationLabware.labwareType.layout.locations.size()
        if (sourceReceptaclesCount > destinationReceptaclesCount) {
            throw new TransferException(
                "The destination layout should be four times the source layout")
        }

        if (destinationLabware.receptacles.size() > 0) {
            def materials = destinationLabware.receptacles.findAll {
                it.materialUuid != null
            }
            if (materials.size() > 0) {
                throw new TransferException(
                    "The destination plate should be empty")
            }
        }
    }

    private static validateLocations(receptacles) {
        def occupiedReceptacles = receptacles.findAll { it.materialUuid != null }

        if (occupiedReceptacles.size() > 0) {
            throw new TransferException(
                "The following locations already occupied in the destination labware: ${occupiedReceptacles*.location.name.join(', ')}")
        }
    }
}
