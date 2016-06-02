/**
 * See README.md for copyright details
 */
package models;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

/**
 * Model for the {@code Labware} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("labwares")
class Labware extends BaseModel {

    @Id
    String id;
    String barcode;
    String external_id;

    @Relationship("labware_type")
    LabwareType labware_type;
}
