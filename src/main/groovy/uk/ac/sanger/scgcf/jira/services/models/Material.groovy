/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models;

import java.util.List;

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

import uk.ac.sanger.scgcf.jira.services.converters.MaterialBatchConverter
import uk.ac.sanger.scgcf.jira.services.models.MaterialBatch
import uk.ac.sanger.scgcf.jira.services.models.Metadatum
import uk.ac.sanger.scgcf.jira.services.models.Material
import uk.ac.sanger.scgcf.jira.services.models.MaterialType
import uk.ac.sanger.scgcf.jira.services.utils.RestService
import uk.ac.sanger.scgcf.jira.services.utils.RestServiceConfig

/**
 * Model for the {@code Material} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("materials")
class Material extends BaseModel {

    @Id
    String id
    String name

    @Relationship("material_type")
    MaterialType materialType

    @Relationship("metadata")
    List<Metadatum> metadata

    @Relationship("parents")
    List<Material> parents

    @Relationship("children")
    List<Material> children

    static RestService restService = RestService.MATERIAL_SERVICE

    def static getMaterials(materialUuids) {
        postMaterials(materialUuids.collect { new Material(id: it) })
    }

    def static create(materialName, materialType, List<Metadatum> metadata = []) {
        def material = new Material(
            name: materialName,
            materialType: new MaterialType(name: materialType),
            metadata: metadata
        )
        postMaterials([material])
    }

    def static postMaterials(List<Material> materials) {
        def postJson = MaterialBatchConverter.convertObjectToJson(new MaterialBatch(materials: materials))
        def returnJson = restService.post(RestServiceConfig.materialBatchPath, postJson)
        MaterialBatchConverter.convertJsonToObject(returnJson).materials
    }
}
