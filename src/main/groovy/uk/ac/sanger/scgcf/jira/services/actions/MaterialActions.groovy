/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.actions

import uk.ac.sanger.scgcf.jira.services.converters.MaterialBatchConverter
import uk.ac.sanger.scgcf.jira.services.models.Material
import uk.ac.sanger.scgcf.jira.services.models.MaterialBatch
import uk.ac.sanger.scgcf.jira.services.utils.RestService
import uk.ac.sanger.scgcf.jira.services.utils.RestServiceConfig

/**
 * The {@code MaterialActions} class represents a class for material related uk.ac.sanger.scgcf.jira.services.actions.
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
