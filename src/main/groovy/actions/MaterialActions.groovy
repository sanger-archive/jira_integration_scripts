/**
 * See README.md for copyright details
 */
package actions

import converters.MaterialBatchConverter
import exceptions.PlateNotFoundException
import groovy.json.JsonBuilder
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

        def materailBatchesJson = restService.post(RestServiceConfig.materialBatchesPath, materialUuidsPayload)
        MaterialBatchConverter.convertJsonToObject(materailBatchesJson).materials
    }
    
}
