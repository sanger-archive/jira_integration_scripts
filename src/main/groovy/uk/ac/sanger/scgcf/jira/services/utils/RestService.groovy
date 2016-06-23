/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.utils

import groovyx.net.http.HTTPBuilder
import uk.ac.sanger.scgcf.jira.services.exceptions.RestServiceException

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.*

/**
 * The {@code RestService} class represents a utility class that can communicate
 * with REST services with GET/POST/UPDATE HTTP methods.
 *
 * @author ke4
 *
 */
class RestService {

    def http

    RestService(serviceUrl) {
        http = new HTTPBuilder(serviceUrl)
    }

    private String request(Map params) {
        http.request(params.requestType) {
            uri.path = params.path
            uri.query = params.query
            requestContentType = JSON
            if (params.json)
                body = params.json

            response.success = { resp ->
                resp.getEntity().getContent().text
            }

            response.failure = { resp ->
                throw new RestServiceException(resp.getEntity().getContent().text)
            }
        }
    }

    def post(path, payload) {
        request(requestType: POST, path: path, json: payload)
    }

    def get(path, queryParams) {
        request(requestType: GET, path: path, query: queryParams)
    }

    def put(path, payload) {
        request(requestType: PUT, path: path, json: payload)
    }

    static CONTAINER_SERVICE = new RestService(RestServiceConfig.containerServiceUrl)
}