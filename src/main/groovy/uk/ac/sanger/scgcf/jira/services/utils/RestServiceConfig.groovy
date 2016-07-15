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

    static String containerServiceUrl
    static String materialServiceUrl
    static String labwarePath
    static String materialBatchPath

    /**
     *
     * @return The containerServiceUrl set in the environment variables
     */
    static String getContainerServiceUrl() {
        getEnvVariable('containerServiceUrl')
    }

    /**
     *
     * @return The materialServiceUrl set in the environment variables
     */
    static String getMaterialServiceUrl() {
        getEnvVariable('materialServiceUrl')
    }

    /**
     *
     * @return The labwarePath from the config file
     */
    static String getLabwarePath() {
        getEnvVariable('labwarePath')
    }

    /**
     *
     * @return The materialBatchPath from the config file
     */
    static String getMaterialBatchPath() {
        getEnvVariable("materialBatchPath")
    }

    static getEnvVariable(envName) {
        def envVariable = System.getenv(envName)
        if (envVariable == null) {
            throw new IllegalStateException("The $envName environmental variable has not been set.")
        }
        envVariable
    }
}
