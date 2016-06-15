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

    final static Map<String, String> restConf;

    static {
        InputStream is = getClass().getResourceAsStream('/utils/rest_service_config.groovy')
        restConf = new ConfigSlurper().parse(is.getText()).flatten()
    }

    String containerServiceUrl
    String materialServiceUrl
    String labwarePath
    String materialBatchPath

    static String getContainerServiceUrl() {
        restConf.get("containerServiceUrl")
    }

    static String getMaterialServiceUrl() {
        restConf.get("materialServiceUrl")
    }

    static String getLabwarePath() {
        restConf.get("labwarePath")
    }

    static String getMaterialBatchPath() {
        restConf.get("materialBatchPath")
    }
}
