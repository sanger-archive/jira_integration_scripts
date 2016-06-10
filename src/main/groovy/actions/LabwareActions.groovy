/**
 * See README.md for copyright details
 */
package actions

import converters.LabwareConverter
import exceptions.PlateNotFoundException
import models.Labware
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

    def static restService = new RestService(RestServiceConfig.containerServiceUrl)

    def static newLabware(Map barcodeMap, type, externalId) {
        def payloadForLabwareCreation = [
            data: [
                attributes: [
                        barcode       : barcodeMap.barcode,
                        barcode_info  : barcodeMap.barcode_info,
                        barcode_prefix: barcodeMap.barcode_prefix,
                        external_id   : externalId
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
        def labwareJson = restService.post(RestServiceConfig.labwarePath, payloadForLabwareCreation)
        LabwareConverter.convertJsonToObject(labwareJson);
    }

    def static getLabwareByBarcode(barcode) {
        def labwareJson = restService.get(RestServiceConfig.labwarePath, [barcode: barcode])

        def foundLabware = LabwareConverter.convertJsonToObjectCollection(labwareJson)[0]

        if (foundLabware == null) {
            throw new PlateNotFoundException("The plate with $barcode could not be found.")
        }

        foundLabware
    }

    def static updateLabware(Labware labware) {
        def labwareJson = LabwareConverter.convertObjectToJson(labware)

        def newLabware = restService.put(RestServiceConfig.labwarePath + labware.id, labwareJson)

        LabwareConverter.convertJsonToObject(newLabware)
    }
}
