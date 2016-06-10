/**
 * See README.md for copyright details
 */
package models;

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

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
}
