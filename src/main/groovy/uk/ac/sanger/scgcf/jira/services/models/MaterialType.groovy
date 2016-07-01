/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models;

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

/**
 * The model used to represent the {@code MaterialType} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("material_types")
class MaterialType extends BaseModel {

    @Id
    String id;
    String name;
}
