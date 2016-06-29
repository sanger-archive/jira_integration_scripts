/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models;

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type;

/**
 * The model used to represent the {@code Metadatum} entity used by the JSON API Converter.
 *
 * @author ke4
 *
 */
@Type("metadata")
class Metadatum extends BaseModel {

    @Id
    String id;
    String key;
    String value;

    boolean equals(Object obj) {
        if (obj == null || obj.class != this.class) {
            false
        } else {
            obj = (Metadatum) obj
            this.key == obj.key && this.value == obj.value
        }
    }
}
