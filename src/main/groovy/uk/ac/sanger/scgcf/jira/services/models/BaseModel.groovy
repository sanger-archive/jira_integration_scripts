/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models
/**
 * Base model for the material and container related uk.ac.sanger.scgcf.jira.services.models.
 * 
 * @author ke4
 *
 */
abstract class BaseModel {

    Date created_at
    List<String> warnings = []

}
