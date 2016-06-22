/**
 * See README.md for copyright details
 */
package actions

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

        (0..sourceLabwares.size() - 1).each { plateNumber ->
            def sourceLabware = sourceLabwares[plateNumber]
            def transferMap = createTransferMapForCombine(sourceLabware.labwareType.layout, plateNumber)

            destinationLabware = transfer(sourceLabware, destinationLabware,
                newMetadataToLocation, materialType, copyMetadata, transferMap)
        }

        updateLabware(destinationLabware)
    }

    def static pool(Labware sourceLabware, Labware destinationLabware,
                    MaterialType materialType, List<String> locations,
                    List<Metadatum> newMetadata = []) {

        if (destinationLabware.labwareType.name != 'generic tube') {
            throw new TransferException('The destination labware should be a tube')
        }


        def missingSourceLocations = locations.findAll { !(it in sourceLabware.receptacles*.location.name) }

        if (missingSourceLocations.size() > 0) {
            throw new TransferException("The source labware does not have these locations: ${missingSourceLocations.join(', ')}")
        }

        def destinationLocation = destinationLabware.receptacles[0].location.name
        def transferMap = locations.collect { location -> [location, destinationLocation] }

        destinationLabware = transfer(sourceLabware, destinationLabware, [(destinationLocation): newMetadata], materialType, [], transferMap)

        updateLabware(destinationLabware)
    }

    private static Labware transfer(Labware sourceLabware, Labware destinationLabware,
                                    Map<String, List<Metadatum>> newMetadataToLocation,
                                    MaterialType materialType,
                                    List<String> copyMetadata,
                                    List<List<String>> transferMap) {
        def transferHashMap = [:]
        transferMap.each { pair ->
            if (!(transferHashMap.containsKey(pair[1]))) {
                transferHashMap[pair[1]] = []
            }
            transferHashMap[pair[1]] << pair[0]
        }

        def receptacleMap = transferHashMap.collectEntries { destination, sourceList ->
            [
                destinationLabware.receptacles.find {
                    it.location.name == destination
                },
                sourceList.collect { sourceLocation ->
                    sourceLabware.receptacles.find {
                        it.location.name == sourceLocation
                    }
                }
            ]
        }

        validateLocations(receptacleMap.keySet())

        def sourceReceptacles = receptacleMap.values().flatten().unique()
        def emptySources = sourceReceptacles.findAll { it.materialUuid == null }

        def materialUuids = (sourceReceptacles*.materialUuid).findAll()
        def sourceMaterials = getMaterialsByUuid(materialUuids)
        def sourceMaterialsByUuid = sourceMaterials.collectEntries { [it.id, it] }
        def materialNameToLocationName = new HashMap<String, String>()

        def destinationMaterials = receptacleMap.collect { destinationReceptacle, sourceReceptacleList ->
            def locationName = destinationReceptacle.location.name
            def newMetadataToAdd = newMetadataToLocation[locationName] ?: []
            def parentMaterials = sourceReceptacleList.collect { sourceMaterialsByUuid[it.materialUuid] }.findAll()
            if (parentMaterials.size() > 0) {
                def childMaterial =
                    createNewChildMaterial("${destinationLabware.barcode}_${locationName}",
                        materialType, parentMaterials,
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

        if (emptySources.size() > 0) {
            destinationLabware.warnings << "The listed location(s) was empty in the source labware: ${(emptySources*.location.name).join(', ')}"
        }

        destinationLabware
    }

    private static getMaterialsByUuid(materialUuids) {
        MaterialActions.getMaterials(materialUuids)
    }

    private static createNewChildMaterial(materialName, type, sourceMaterials,
                                          copyMetadata, newMetadata) {
        new Material(
            name: materialName,
            materialType: type,
            metadata: sourceMaterials[0].metadata.findAll {
                it.key in copyMetadata
            } + newMetadata,
            parents: sourceMaterials
        )
    }

    private static postNewMaterials(destinationMaterials) {
        MaterialActions.postMaterials(destinationMaterials)
    }

    private static updateLabware(destinationLabware) {
        LabwareActions.updateLabware(destinationLabware)
    }

    private static createTransferMapForCombine(sourceLayout, plateNumber) {
        def transferMap = []

        (1..sourceLayout.row).each { row ->
            (1..sourceLayout.column).each { column ->
                def sourceRow = (char) (64 + row)
                def sourceColumn = column

                def destinationRow = (char) (64 + (row * 2) - (((int) (plateNumber / 2)) ? 0 : 1))
                def destinationColumn = column * 2 - (plateNumber % 2 ? 0 : 1)

                transferMap << [
                    "$sourceRow$sourceColumn",
                    "$destinationRow$destinationColumn"
                ]
            }
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
        def occupiedReceptacles = receptacles.findAll { it.materialUuid != null }.unique()

        if (occupiedReceptacles.size() > 0) {
            throw new TransferException(
                "The following locations already occupied in the destination labware: ${occupiedReceptacles*.location.name.join(', ')}")
        }
    }
}
