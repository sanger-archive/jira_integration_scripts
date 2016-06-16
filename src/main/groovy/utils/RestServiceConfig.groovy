/**
 * See README.md for copyright details
 */
package utils

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
        InputStream is = getClass().getResourceAsStream('/utils/rest_service_config.groovy')
        restConf = new ConfigSlurper().parse(is.getText()).flatten()
    }

    static String containerServiceUrl
    static String materialServiceUrl
    static String labwarePath
    static String materialBatchPath

    static String getContainerServiceUrl() {
        containerServiceUrl = System.getenv('containerServiceUrl')
        if (containerServiceUrl == null) {
            throw new IllegalStateException("The containerServiceUrl environmental variable has not been set.")
        }
        containerServiceUrl
    }

    static String getMaterialServiceUrl() {
        materialServiceUrl = System.getenv('materialServiceUrl')
        if (materialServiceUrl == null) {
            throw new IllegalStateException("The materialServiceUrl environmental variable has not been set.")
        }
        materialServiceUrl
    }

    static String getLabwarePath() {
        restConf.get("labwarePath")
    }

    static String getMaterialBatchPath() {
        restConf.get("materialBatchPath")
    }
}
