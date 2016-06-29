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
 * The model used to represent the {@code Labware} entity used by the JSON API Converter.
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

    /**
     * Create a new {@code Labware} object in the database.
     * @param barcode The barcode for the new {@code Labware} object, cannot be supplied with {@code barcodePrefix} or {@code barcodeInfo}
     * @param barcodePrefix The start of the barcode if it's being generated.
     * @param barcodeInfo The middle of the barcode if it's being generated. Optional.
     * @param labwareType The type of the new {@code Labware}
     * @param externalId The external ID of the new {@code Labware}. Must be unique
     * @param metadata The {@code Metadaum} objects to be added to the new object
     * @return The persisted {@code Labware} object
     */
    static Labware create(Map barcodeMap, LabwareType labwareType, String externalId, List<Metadatum> metadata = []) {
        def labware = new Labware(
            barcode: barcodeMap.barcode,
            barcodeInfo: barcodeMap.barcodeInfo,
            barcodePrefix: barcodeMap.barcodePrefix,
            externalId: externalId,
            labwareType: labwareType,
            metadata: metadata
        )

        def postJson = LabwareConverter.convertObjectToJson(labware)
        def labwareJson = restService.post(RestServiceConfig.labwarePath, postJson)
        LabwareConverter.convertJsonToObject(labwareJson)
    }

    /**
     * Retrieve a {@code Labware} from the database, or throw {@code LabwareNotFoundException}
     * @param barcode The barcode of the {@code Labware} to find
     * @return The {@code Labware} object
     */
    static Labware findByBarcode(String barcode) {
        def labwareJson = restService.get(RestServiceConfig.labwarePath, [barcode: barcode])

        def foundLabware = LabwareConverter.convertJsonToObjectCollection(labwareJson)[0]

        if (foundLabware == null) {
            throw new LabwareNotFoundException("The labware with $barcode could not be found.")
        }

        foundLabware
    }

    /**
     * Save the changed made to this {@code Labware} object.
     * @return this
     */
    Labware update() {
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

    /**
     *
     * @return The non-null material IDs in this object.
     */
    List<String> materialUuids() {
        receptacles*.materialUuid.findAll { it != null }
    }
}
