/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models;

import java.time.LocalDate;

/**
 * Base model for the material and container related uk.ac.sanger.scgcf.jira.services.models.
 * 
 * @author ke4
 *
 */
abstract class BaseModel {

    LocalDate created_at
    List<String> warnings = []
}
