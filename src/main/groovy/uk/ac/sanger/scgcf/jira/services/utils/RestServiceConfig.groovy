/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.utils

import groovy.json.JsonSlurper

import java.nio.file.Paths

/**
 * The {@code RestServiceConfig} class represents a configuration object that
 * holds the REST service related configuration settings.
 * 
 * @author ke4
 *
 */
class RestServiceConfig {

    static Map<String, String> restConf

    static {
        URL url = this.class.getResource('/config/rest_service_config.json')
        JsonSlurper jsonSlurper = new JsonSlurper()
        Paths.get(url.toURI()).withReader { Reader reader ->
            restConf = jsonSlurper.parse(reader)
        }
    }

    static String containerServiceUrl
    static String materialServiceUrl
    static String labwarePath
    static String materialBatchPath

    /**
     *
     * @return The containerServiceUrl set in the environment variables
     */
    static String getContainerServiceUrl() {
        containerServiceUrl = System.getenv('containerServiceUrl')
        if (containerServiceUrl == null) {
            throw new IllegalStateException("The containerServiceUrl environmental variable has not been set.")
        }
        containerServiceUrl
    }

    /**
     *
     * @return The materialServiceUrl set in the environment variables
     */
    static String getMaterialServiceUrl() {
        materialServiceUrl = System.getenv('materialServiceUrl')
        if (materialServiceUrl == null) {
            throw new IllegalStateException("The materialServiceUrl environmental variable has not been set.")
        }
        materialServiceUrl
    }

    /**
     *
     * @return The labwarePath from the config file
     */
    static String getLabwarePath() {
        restConf.get("labwarePath")
    }

    /**
     *
     * @return The materialBatchPath from the config file
     */
    static String getMaterialBatchPath() {
        restConf.get("materialBatchPath")
    }
}
