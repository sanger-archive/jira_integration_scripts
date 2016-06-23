/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import uk.ac.sanger.scgcf.jira.services.converters.LabwareConverter
import uk.ac.sanger.scgcf.jira.services.exceptions.LabwareNotFoundException
import uk.ac.sanger.scgcf.jira.services.utils.RestService
import uk.ac.sanger.scgcf.jira.services.utils.RestServiceConfig

/**
 * Model for the {@code Labware} entity used by the JSON API Converter.
 *
 * @author ke4
 *
 */
@Type("labwares")
class Labware extends BaseModel {

    @Id
    String id
    String barcode
    @JsonProperty('external_id')
    String externalId

    @Relationship("labware_type")
    LabwareType labwareType

    @Relationship("receptacles")
    List<Receptacle> receptacles

    @Relationship("metadata")
    List<Metadatum> metadata

    static RestService restService = RestService.CONTAINER_SERVICE

    static create(Map barcodeMap, LabwareType labwareType, String externalId, List<Metadatum> metadata = []) {
        def payloadForLabwareCreation = [
            data: [
                attributes: [
                    barcode: barcodeMap.barcode,
                    barcode_info: barcodeMap.barcode_info,
                    barcode_prefix: barcodeMap.barcode_prefix,
                    external_id: externalId
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: labwareType.name
                            ]
                        ]
                    ],
                    metadata: [
                        data: metadata.collect {
                            [
                                attributes: [
                                    key: it.key,
                                    value: it.value
                                ]
                            ]
                        }
                    ]
                ]
            ]
        ]
        def labwareJson = restService.post(RestServiceConfig.labwarePath, payloadForLabwareCreation)
        LabwareConverter.convertJsonToObject(labwareJson)
    }

    def static findByBarcode(barcode) {
        def labwareJson = restService.get(RestServiceConfig.labwarePath, [barcode: barcode])

        def foundLabware = LabwareConverter.convertJsonToObjectCollection(labwareJson)[0]

        if (foundLabware == null) {
            throw new LabwareNotFoundException("The labware with $barcode could not be found.")
        }

        foundLabware
    }

    def update() {
        def labwareJson = LabwareConverter.convertObjectToJson(this)
        def newLabware = restService.put(RestServiceConfig.labwarePath + this.id, labwareJson)
        newLabware = LabwareConverter.convertJsonToObject(newLabware)

        this.id = newLabware.id
        this.barcode = newLabware.barcode
        this.externalId = newLabware.externalId
        this.labwareType = newLabware.labwareType
        this.receptacles = newLabware.receptacles
        this.metadata = newLabware.metadata

        this
    }

    def materialUuids() {
        receptacles*.materialUuid.findAll { it != null }
    }
}
