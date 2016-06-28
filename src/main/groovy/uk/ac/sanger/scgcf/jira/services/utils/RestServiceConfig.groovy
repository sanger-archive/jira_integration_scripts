/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.utils

/**
 * The {@code RestServiceConfig} class represents a configuration object that
 * holds the REST service related configuration settings.
 * 
 * @author ke4
 *
 */
class RestServiceConfig {

    final static Map<String, String> restConf

    static {
        InputStream is = getClass().getResourceAsStream('/uk/ac/sanger/scgcf/jira/services/utils/rest_service_config.groovy')
        restConf = new ConfigSlurper().parse(is.getText()).flatten()
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
