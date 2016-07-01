/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * The model used to represent the {@code LabwareType} entity used by the JSON API Converter.
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

    @Override
    def boolean equals(other) {
        this.name == other.name
    }
}

enum LabwareTypes {
    static final LabwareType GENERIC_TUBE = new LabwareType(name: 'generic tube')
    static final LabwareType GENERIC_96_PLATE = new LabwareType(name: 'generic 96 well plate')
    static final LabwareType GENERIC_384_PLATE = new LabwareType(name: 'generic 384 well plate')
}
