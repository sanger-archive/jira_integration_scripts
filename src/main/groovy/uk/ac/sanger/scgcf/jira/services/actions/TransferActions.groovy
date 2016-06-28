/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.actions

import uk.ac.sanger.scgcf.jira.services.exceptions.TransferException
import uk.ac.sanger.scgcf.jira.services.models.*

/**
 * A collection of methods for transferring {@code Material}s between {@code Labware} objects
 *
 * @author rf9
 * @author ke4
 *
 */
class TransferActions {

    /**
     * A generic transfer from any number of {@code Labware}s to any number of {@code Labware}s
     * @param sourceLabwares The {@code Labware}s to be transferred from
     * @param destinationLabwares The {@code Labware}s to be transferred into. Modified.
     * @param materialType The {@MaterialType} of the created {@code Material}s
     * @param transferMap A collection of {@code Mapping} objects to define the transfer
     * @param copyMetadata The {@code Metadatum} keys to be copied over
     * @param newMetadataToLocation A map from {@code Location} names to {@code Metadatum} to be added to the destinations
     * @return The {@code destinationLabwares}
     */
    def static cherrypick(Collection<Labware> sourceLabwares, Collection<Labware> destinationLabwares,
                          MaterialType materialType, Collection<TransferActions> transferMap,
                          Collection<String> copyMetadata = [], Map<String, Collection<Metadatum>> newMetadataToLocation = [:]) {
        transfer(sourceLabwares, destinationLabwares, newMetadataToLocation, materialType, copyMetadata, transferMap)
    }

    /**
     * A stamp from a single {@code Labware} into a single {@code Labware}.
     * Both {@code Labware} objects must have the same {@code Layout}.
     * Each {@code Material} in the {@code sourceLabware} will be copied into the well of the same
     * name in the {@code destinationLabware}
     * @param sourceLabware The {@code Labware} object to be transferred from
     * @param destinationLabware The {@code Labware} object to be transferred into
     * @param materialType The {@code MaterialType} of the created {@code Material}s
     * @param copyMetadata The {@code Metadatum} keys to be copied over
     * @param newMetadataToLocation A map from {@code Location} names to {@code Metadatum} to be added to the destinations
     * @return The {@code destinationLabware}
     */
    def static stamp(Labware sourceLabware, Labware destinationLabware,
                     MaterialType materialType, Collection<String> copyMetadata = [],
                     Map<String, Collection<Metadatum>> newMetadataToLocation = [:]) {

        if (sourceLabware.labwareType.layout != destinationLabware.labwareType.layout)
            throw new TransferException(
                "Labwares must have the same layout. ${sourceLabware.labwareType.layout.name} and ${destinationLabware.labwareType.layout.name}")

        def transferMap = sourceLabware.receptacles.collect {
            new Mapping(sourceBarcode: sourceLabware.barcode, sourceLocation: it.location.name,
                destinationBarcode: destinationLabware.barcode, destinationLocation: it.location.name)
        }
        def destinationLabwares = transfer([sourceLabware], [destinationLabware], newMetadataToLocation, materialType, copyMetadata, transferMap)
        destinationLabwares[0]
    }

    /**
     * A stamp from a single {@code Labware} into a single {@code Labware}.
     * Both {@code Labware} objects must have the same {@code Layout}.
     * Each {@code Material} in the {@code sourceLabware} will be copied into the well of the same
     * name in the {@code destinationLabware} if the well name is in the list
     * @param sourceLabware The {@code Labware} object to be transferred from
     * @param destinationLabware The {@code Labware} object to be transferred into
     * @param materialType The {@code MaterialType} of the created {@code Material}s
     * @param destinationLocations The list of {@code Location} names to be copied over
     * @param copyMetadata The {@code Metadatum} keys to be copied over
     * @param newMetadataToLocation A map from {@code Location} names to {@code Metadatum} to be added to the destinations
     * @return The {@code destinationLabware}
     */
    def static selectiveStamp(Labware sourceLabware, Labware destinationLabware,
                              MaterialType materialType, Collection<String> destinationLocations,
                              Collection<String> copyMetadata = [], Map<String, Collection<Metadatum>> newMetadataToLocation = [:]) {

        if (sourceLabware.labwareType.layout != destinationLabware.labwareType.layout)
            throw new TransferException(
                "Labwares must have the same layout. ${sourceLabware.labwareType.layout.name} and ${destinationLabware.labwareType.layout.name}")

        def transferMap = destinationLocations.collect {
            new Mapping(sourceBarcode: sourceLabware.barcode, sourceLocation: it,
                destinationBarcode: destinationLabware.barcode, destinationLocation: it)
        }
        def destinationLabwares = transfer([sourceLabware], [destinationLabware], newMetadataToLocation, materialType, copyMetadata, transferMap)
        destinationLabwares[0]
    }

    /**
     * A transfer from a single {@code Receptacle} into every {@code Receptacle} in the destination
     * @param sourceLabware The {@code Labware} to be transferred from
     * @param destinationLabware The {@code Labware} to be transferred into
     * @param materialType The {@code MaterialType} of the created {@code Material}s
     * @param destinationLocations The list of {@code Location} names to be copied into
     * @param copyMetadata The {@code Metadatum} keys to be copied over
     * @param newMetadataToLocation A map from {@code Location} names to {@code Metadatum} to be added to the destinations
     * @return The {@code destinationLabware}
     */
    def static split(Labware sourceLabware, Labware destinationLabware,
                     MaterialType materialType, Collection<String> destinationLocations,
                     Collection<String> copyMetadata = [],
                     Map<String, Collection<Metadatum>> newMetadataToLocation = [:]) {

        def transferMap = destinationLocations.collect {
            new Mapping(sourceBarcode: sourceLabware.barcode, sourceLocation: sourceLabware.receptacles[0].location.name,
                destinationBarcode: destinationLabware.barcode, destinationLocation: it)
        }
        def destinationLabwares = transfer([sourceLabware], [destinationLabware], newMetadataToLocation, materialType, copyMetadata, transferMap)
        destinationLabwares[0]
    }

    /**
     * A transfer from four source {@code Labware}s in to a single {@code Labware} with four times the number of {@code Receptacle}s
     * @param sourceLabwares Up to four {@code Labware}s to be transferred from. Ordered by quadrant
     * @param destinationLabware The {@code Labware} to be transferred into
     * @param materialType The {@code MaterialType} of the created {@code Material}s
     * @param copyMetadata The {@code Metadatum} keys to be copied over
     * @param newMetadataToLocation A map from {@code Location} names to {@code Metadatum} to be added to the destinations
     * @return The {@code destinationLabware}
     */
    def static combine(List<Labware> sourceLabwares, Labware destinationLabware,
                       MaterialType materialType, Collection<String> copyMetadata = [],
                       Map<String, Collection<Metadatum>> newMetadataToLocation = [:]) {

        validateCombineParameters(sourceLabwares, destinationLabware)

        def transferMap = []
        sourceLabwares.eachWithIndex { sourceLabware, plateNumber ->
            def layout = sourceLabware.labwareType.layout

            transferMap += (1..layout.row).collect { row ->
                (1..layout.column).collect { column ->
                    def sourceRow = (char) (64 + row)
                    def sourceColumn = column

                    def destinationRow = (char) (64 + (row * 2) - (((int) (plateNumber / 2)) ? 0 : 1))
                    def destinationColumn = column * 2 - (plateNumber % 2 ? 0 : 1)

                    new Mapping(
                        sourceBarcode: sourceLabware.barcode,
                        sourceLocation: "$sourceRow$sourceColumn",
                        destinationLocation: "$destinationRow$destinationColumn",
                        destinationBarcode: destinationLabware.barcode
                    )
                }
            }.flatten()
        }
        def destinationLabwares = transfer(sourceLabwares, [destinationLabware], newMetadataToLocation, materialType, copyMetadata, transferMap)
        destinationLabwares[0]
    }

    /**
     * A transfer from a set of {@code Receptacle}s in a single {@code Labware} into a single {@code Receptacle}
     * @param sourceLabware The {@code Labware} to be transferred from
     * @param destinationLabware The {@code Labware} to be transferred into. Must be of {@code Layout} {@code "generic tube"}
     * @param materialType The {@code MaterialType} of the created {@code Material}
     * @param locations The {@code Location} names to be pooled from
     * @param newMetadata A collection of {@code Metadata} to be added to the created {@code Material}
     * @return The {@code destinationLabware}
     */
    def static pool(Labware sourceLabware, Labware destinationLabware,
                    MaterialType materialType, Collection<String> locations,
                    Collection<Metadatum> newMetadata = []) {

        if (destinationLabware.labwareType.name != LabwareTypes.GENERIC_TUBE.name) {
            throw new TransferException("The destination labware should be a $LabwareTypes.GENERIC_TUBE.name")
        }

        def destinationLocation = destinationLabware.receptacles[0].location.name
        def transferMap = locations.collect { location ->
            new Mapping(sourceBarcode: sourceLabware.barcode, sourceLocation: location,
                destinationBarcode: destinationLabware.barcode, destinationLocation: destinationLocation
            )
        }

        def destinationLabwares = transfer([sourceLabware], [destinationLabware], [(destinationLocation): newMetadata], materialType, [], transferMap)
        destinationLabwares[0]
    }

    private static transfer(Collection<Labware> sourceLabwares, Collection<Labware> destinationLabwares,
                            Map<String, Collection<Metadatum>> newMetadataToLocation,
                            MaterialType materialType,
                            Collection<String> copyMetadata,
                            Collection<Mapping> transferMap) {

        def mappingStringToReceptacle = [:]
        (sourceLabwares + destinationLabwares).each { labware ->
            labware.receptacles.each { receptacle ->
                mappingStringToReceptacle["${labware.barcode}${receptacle.location.name}"] = receptacle
            }
        }

        def missingSources = []
        def missingDestinations = []

        def receptaclePairs = transferMap.collect { transferMapping ->
            def sourceReceptacle = mappingStringToReceptacle["${transferMapping.sourceBarcode}${transferMapping.sourceLocation}"]
            def destinationReceptacle = mappingStringToReceptacle["${transferMapping.destinationBarcode}${transferMapping.destinationLocation}"]

            if (sourceReceptacle == null) {
                missingSources << "${transferMapping.sourceBarcode} ${transferMapping.sourceLocation}"
            }
            if (destinationReceptacle == null) {
                missingDestinations << "${transferMapping.destinationBarcode} ${transferMapping.destinationLocation}"
            }

            [sourceReceptacle, destinationReceptacle]
        }

        if (missingSources.size() > 0) {
            throw new TransferException("The source labwares do not have these locations: ${missingSources.join(', ')}")
        }
        if (missingDestinations.size() > 0) {
            throw new TransferException("The destinations labwares do not have these locations: ${missingDestinations.join(', ')}")
        }

        def receptacleMap = [:]
        receptaclePairs.each { receptaclePair ->
            if (!(receptacleMap.containsKey(receptaclePair[1]))) {
                receptacleMap[receptaclePair[1]] = []
            }
            receptacleMap[receptaclePair[1]] << receptaclePair[0]
        }

        def receptacleToLabware = [:]
        (sourceLabwares + destinationLabwares).each { labware ->
            labware.receptacles.each { receptacle ->
                receptacleToLabware[receptacle] = labware
            }
        }

        validateLocations(receptacleMap.keySet())

        def sourceReceptacles = receptacleMap.values().flatten().unique()

        def materialUuids = (sourceReceptacles*.materialUuid).findAll()
        def sourceMaterials = getMaterialsByUuid(materialUuids)
        def sourceMaterialsByUuid = sourceMaterials.collectEntries { [it.id, it] }
        def materialNameToReceptacle = new HashMap<String, Receptacle>()

        def destinationMaterials = receptacleMap.collect { destinationReceptacle, sourceReceptacleList ->
            def locationName = destinationReceptacle.location.name
            def newMetadataToAdd = newMetadataToLocation[locationName] ?: []
            def parentMaterials = sourceReceptacleList.collect { sourceMaterialsByUuid[it.materialUuid] }.findAll()
            if (parentMaterials.size() > 0) {
                def childMaterial =
                    createNewChildMaterial("${receptacleToLabware[destinationReceptacle].barcode}_${locationName}",
                        materialType, parentMaterials,
                        copyMetadata, newMetadataToAdd)
                materialNameToReceptacle[childMaterial.name] = destinationReceptacle
                childMaterial
            } else {
                null
            }
        }.findAll()
        destinationMaterials = postNewMaterials(destinationMaterials)

        destinationMaterials.each { material ->
            def receptacle = materialNameToReceptacle[material.name]
            receptacle.materialUuid = material.id
        }

        for (Labware destinationLabware : destinationLabwares) {
            updateLabware(destinationLabware)
        }

        def emptySourceReceptacles = sourceReceptacles.findAll { it.materialUuid == null }
        if (emptySourceReceptacles.size() > 0) {
            def emptySourcesByLabware = emptySourceReceptacles.groupBy { receptacleToLabware[it] }

            def warnings = []
            emptySourcesByLabware.each { labware, receptacles ->
                warnings << "These locations in $labware.barcode are empty: ${(receptacles*.location.name).join(', ')}"
            }
            destinationLabwares.each { it.warnings += warnings }
        }

        destinationLabwares
    }

    private static getMaterialsByUuid(materialUuids) {
        Material.getMaterials(materialUuids)
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
        Material.postMaterials(destinationMaterials)
    }

    private static updateLabware(destinationLabware) {
        destinationLabware.update()
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

    /**
     * Struct for defining a receptacle transfer
     */
    static class Mapping {

        String sourceBarcode
        String sourceLocation
        String destinationBarcode
        String destinationLocation
    }
}
