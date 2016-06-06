/**
 * See README.md for copyright details
 */
package models

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * Model for the {@code Receptacle} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("receptacles")
class Receptacle extends BaseModel {

    @Id
    String id;
    @JsonProperty('material_uuid')
    String materialUuid;

    @Relationship("labware")
    Labware labware;

    @Relationship("location")
    Location location;
}
