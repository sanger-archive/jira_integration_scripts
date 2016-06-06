/**
 * See README.md for copyright details
 */
package models;

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * Model for the {@code MaterialBatch} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("material_batches")
class MaterialBatch extends BaseModel {

    @Id
    String id

    @Relationship("materials")
    List<Material> materials
}
