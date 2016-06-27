/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.exceptions

/**
 * The {@code RestServiceException} class represents an exception wrapper
 * for the error messages coming from the Material or Container REST Service.
 * 
 * @author ke4
 *
 */
class RestServiceException extends Exception {

    RestServiceException(message) {
        super(message)
    }
}
