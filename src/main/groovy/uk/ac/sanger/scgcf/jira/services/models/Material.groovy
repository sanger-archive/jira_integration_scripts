/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import uk.ac.sanger.scgcf.jira.services.converters.MaterialBatchConverter
import uk.ac.sanger.scgcf.jira.services.utils.RestService
import uk.ac.sanger.scgcf.jira.services.utils.RestServiceConfig

/**
 * The model used to represent the {@code Material} entity used by the JSON API Converter.
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

    /**
     * Get a list of {@code Material} objects from the JSON API by their IDs.
     * @param materialUuids The material IDs to find
     * @return The list of materials
     */
    static List<Material> getMaterials(Collection<Material> materialUuids) {
        postMaterials(materialUuids.collect { new Material(id: it) })
    }

    /**
     * Create a new {@code Material} in the database.
     * @param materialName The name of the new {@code Material}
     * @param materialType The name of the new {@code Material}'s type
     * @param metadata A list of metadata object to be added to the {@code Material}
     * @return The persisted {@code Material}
     */
    static Material create(String materialName, String materialType, Collection<Metadatum> metadata = []) {
        def material = new Material(
            name: materialName,
            materialType: new MaterialType(name: materialType),
            metadata: metadata
        )
        postMaterials([material])[0]
    }

    /**
     * Save or update a collection of {@code Material}s in the database.
     * @param materials The {@code Material}s to be persisted. Is not modified.
     * @return The new {@code Material}s
     */
    static List<Material> postMaterials(Collection<Material> materials) {
        def postJson = MaterialBatchConverter.convertObjectToJson(new MaterialBatch(materials: materials))
        def returnJson = restService.post(RestServiceConfig.materialBatchPath, postJson)
        MaterialBatchConverter.convertJsonToObject(returnJson).materials
    }
}
