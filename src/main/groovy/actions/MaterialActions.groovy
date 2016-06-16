/**
 * See README.md for copyright details
 */
package actions

import converters.MaterialBatchConverter
import models.Material
import models.MaterialBatch
import utils.RestService
import utils.RestServiceConfig

/**
 * The {@code MaterialActions} class represents a class for material related actions.
 *
 * @author ke4
 *
 */
class MaterialActions {

    def static restService = new RestService(RestServiceConfig.getMaterialServiceUrl())

    def static getMaterials(materialUuids) {
        postMaterials(materialUuids.collect { new Material(id: it) })
    }

    def static postMaterials(List<Material> materials) {
        def postJson = MaterialBatchConverter.convertObjectToJson(new MaterialBatch(materials: materials))
        def returnJson = restService.post(RestServiceConfig.materialBatchPath, postJson)
        MaterialBatchConverter.convertJsonToObject(returnJson).materials
    }

}
