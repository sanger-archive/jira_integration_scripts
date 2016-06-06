/**
 * See README.md for copyright details
 */
package models

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * Model for the {@code Labware} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("labwares")
class Labware extends BaseModel {

    @Id
    String id
    String barcode
    @JsonProperty('external_id')
    String externalId

    @Relationship("labware_type")
    LabwareType labwareType

    @Relationship("receptacles")
    List<Receptacle> receptacles

    @Relationship("metadata")
    List<Metadatum> metadata

    def materialUuids() {
        receptacles*.materialUuid
    }
}
