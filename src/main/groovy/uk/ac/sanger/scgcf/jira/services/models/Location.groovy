/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * Model for the {@code Location} entity used by the JSON API Converter.
 * 
 * @author ke4
 *
 */
@Type("locations")
class Location extends BaseModel {

    @Id
    String id;
    String name;

    @Relationship("layout")
    Layout layout;

    @Override
    boolean equals(Object obj) {
        this.name == obj.name
    }
}
