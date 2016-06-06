/**
 * See README.md for copyright details
 */
package actions

import converters.MaterialBatchConverter
import utils.RestService
import utils.RestServiceConfig

/**
 * The {@code MaterialActions} class represents a class for material related actions.
 * 
 * @author ke4
 *
 */
class MaterialActions {

    def static restService = new RestService(RestServiceConfig.materialServiceUrl)

    def static getMaterials(materialUuids) {
        def materialUuidsPayload = [
            data: [
                relationships: [
                    materials: [
                        data: materialUuids.collect { [ id: it ] }
                    ]
                ]
            ]
        ]

        def materialBatchesJson = restService.post(RestServiceConfig.materialBatchPath, materialUuidsPayload)
        MaterialBatchConverter.convertJsonToObject(materialBatchesJson).materials
    }
    
}
