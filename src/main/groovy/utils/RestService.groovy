/**
 * See README.md for copyright details
 */
package utils

import exceptions.RestServiceException
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper


/**
 * The {@code RestService} class represents a utility class that can communicate
 * with REST services with GET/POST/UPDATE HTTP methods.
 * 
 * @author ke4
 *
 */
class RestService {

    def http = new HTTPBuilder(RestServiceConfig.serviceUrl)

    def post(path, payload) {
        http.request( POST ) {
            uri.path = path
                requestContentType = JSON
                body =  payload

            response.success = { resp ->
                resp.getEntity().getContent().text
            }

            response.failure = { resp ->
                def slurper = new JsonSlurper()
                def errors = slurper.parseText(resp.getEntity().getContent().text)
                def errorMessage = errors.collect { key, value -> "${key.capitalize()} ${value.join(', ')}" }.join(".${System.getProperty('line.separator')}") 
                throw new RestServiceException(errorMessage)
            }
        }
    }

    def get(path, queryParams) {
        http.request(GET) {
            uri.path = path
            uri.query =  queryParams
            requestContentType = JSON

            response.success = { resp ->
                resp.getEntity().getContent().text
            }

            response.failure = { resp ->
                def slurper = new JsonSlurper()
                def errors = slurper.parseText(resp.getEntity().getContent().text)
                def errorMessage = errors.collect { key, value -> "${key.capitalize()} ${value.join(', ')}" }.join(".${System.getProperty('line.separator')}")
                throw new RestServiceException(errorMessage)
            }
        }
    }
}
