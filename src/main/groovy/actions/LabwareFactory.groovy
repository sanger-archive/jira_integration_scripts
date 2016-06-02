/**
 * See README.md for copyright details
 */
package actions

import converters.LabwareConverter;
import groovy.json.JsonBuilder
import utils.RestService
import utils.RestServiceConfig

/**
 * The {@code LabwareFactory} class represents factory for creating a new labware
 * using the REST API of the Material Service.
 * 
 * @author ke4
 *
 */
class LabwareFactory {

    def static GENERIC_96_WELL_PLATE_TYPE = "generic 96 well plate"

    //TODO pass it to the payload method
    def SINGLE_CELL_BARCODE_PREFIX = "SCGC"

    def external_id;

    def newPlate(String type) {
        def restService = new RestService()
        def plateJson = restService.post(RestServiceConfig.labwarePath, payload(external_id))
        LabwareConverter.convertJson(plateJson);
    }

    def payload(String input_external_id) {

        [
            data: [
                attributes: [
                    barcode_prefix: SINGLE_CELL_BARCODE_PREFIX,
                    external_id: input_external_id
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: GENERIC_96_WELL_PLATE_TYPE
                            ]
                        ]
                    ]
                ]
            ]
        ]
    }
}
