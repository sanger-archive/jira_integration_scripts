/**
 * See README.md for copyright details
 */
package actions

import exceptions.TransferException
import models.Labware
import models.Material
import models.MaterialType

/**
 * The {@code TransferActions} class represents a class for transfer related actions.
 *
 * @author rf9
 *
 */
class TransferActions {

    def static stamp(Labware source, Labware destination, MaterialType materialType, List<String> copyMetadata = []) {
        if (source.labwareType.layout != destination.labwareType.layout)
            throw new TransferException("Labwares must have the same layout. ${source.labwareType.layout.name} and ${destination.labwareType.layout.name}")

        def sourceMaterialUuidToLocation = source.receptacles.collectEntries { [it.materialUuid, it.location] }

        def sourceMaterials = MaterialActions.getMaterials(source.materialUuids())
        def destinationMaterials = sourceMaterials.collect { sourceMaterial ->
            new Material(
                name: "${destination.barcode}_${sourceMaterialUuidToLocation[sourceMaterial.id].name}",
                materialType: materialType,
                metadata: sourceMaterial.metadata.findAll { it.key in copyMetadata },
                parents: [sourceMaterial]
            )
        }
        destinationMaterials = MaterialActions.postMaterials(destinationMaterials)

        destinationMaterials.each { material ->
            def location = sourceMaterialUuidToLocation[material.parents[0].id]
            def receptacle = destination.receptacles.find { it.location == location }
            receptacle.materialUuid = material.id
        }

        LabwareActions.updateLabware(destination)
    }
}
