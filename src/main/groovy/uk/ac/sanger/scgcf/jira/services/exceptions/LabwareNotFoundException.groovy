/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.exceptions

/**
 * @author ke4
 *
 */
class LabwareNotFoundException extends RestServiceException {

    LabwareNotFoundException(message) {
        super(message)
    }
}
