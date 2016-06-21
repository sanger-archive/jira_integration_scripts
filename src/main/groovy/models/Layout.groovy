/**
 * See README.md for copyright details
 */
package models

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

/**
 * Model for the {@code Layout} entity used by the JSON API Converter.
 *
 * @author ke4
 *
 */
@Type("layouts")
class Layout extends BaseModel {

    @Id
    String id
    String name
    Integer row
    Integer column

    @Relationship('locations')
    List<Location> locations

    @Override
    def boolean equals(other) {
        this.name == other.name
    }
}
