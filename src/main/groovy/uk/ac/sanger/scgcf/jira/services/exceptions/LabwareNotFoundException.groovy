/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.exceptions

/**
 * The {@code LabwareNotFoundException} is thrown when a {@code Labware} object
 * cannot be found in the database.
 *
 * @author ke4
 *
 */
class LabwareNotFoundException extends RestServiceException {

    LabwareNotFoundException(message) {
        super(message)
    }
}
