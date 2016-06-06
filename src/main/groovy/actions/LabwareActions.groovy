/**
 * See README.md for copyright details
 */
package actions

import converters.LabwareConverter;
import exceptions.PlateNotFoundException
import groovy.json.JsonBuilder
import utils.RestService
import utils.RestServiceConfig

/**
 * The {@code LabwareActions} class represents factory for creating a new labware
 * using the REST API of the Material Service.
 * 
 * @author ke4
 *
 */
class LabwareActions {

    def static restService = new RestService()

    def static newPlate(Map barcodeMap, type, external_id) {
        def payloadForLabwareCreation = [
            data: [
                attributes: [
                    barcode: barcodeMap.barcode,
                    barcode_info: barcodeMap.barcode_info,
                    barcode_prefix: barcodeMap.barcode_prefix,
                    external_id: external_id
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: type
                            ]
                        ]
                    ]
                ]
            ]
        ]
        def plateJson = restService.post(RestServiceConfig.labwarePath, payloadForLabwareCreation)
        LabwareConverter.convertJsonToObject(plateJson);
    }

    def static getPlateByBarcode(barcode) {
        def path = RestServiceConfig.getLabwarePath()
        def plateJson = restService.get(path, [ barcode: barcode])

        def foundPlate = LabwareConverter.convertJsonToObjectCollection(plateJson)[0]

        if (foundPlate == null) {
            throw new PlateNotFoundException("The plate with $barcode could not be found.")
        }

        foundPlate
    }
}
