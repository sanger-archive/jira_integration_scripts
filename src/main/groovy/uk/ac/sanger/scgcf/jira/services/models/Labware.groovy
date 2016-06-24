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
import uk.ac.sanger.scgcf.jira.services.models.*

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
    String barcodeInfo
    String barcodePrefix
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
        def labware = new Labware(
            barcode: barcodeMap.barcode,
            barcodeInfo: barcodeMap.barcode_info,
            barcodePrefix: barcodeMap.barcode_prefix,
            externalId: externalId,
            labwareType: labwareType,
            metadata: metadata
        )

        def postJson = LabwareConverter.convertObjectToJson(labware)
        def labwareJson = restService.post(RestServiceConfig.labwarePath, postJson)
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
