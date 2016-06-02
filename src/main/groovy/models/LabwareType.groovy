/**
 * See README.md for copyright details
 */
package models;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

/**
 * Model for the {@code LabwareType} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("labware_types")
class LabwareType extends BaseModel {

    @Id
    String id;
    String name;

    @Relationship("layout")
    Layout layout;
}
