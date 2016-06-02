/**
 * See README.md for copyright details
 */
package exceptions

/**
 * The {@code RestServiceException} class represents an exception wrapper
 * for the error messages coming from the Material or Container REST Service.
 * 
 * @author ke4
 *
 */
class RestServiceException extends Exception {

    RestServiceException(String message) {
        super(message)
    }
}
