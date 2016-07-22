/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.converters

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.jasminb.jsonapi.ResourceConverter
import org.apache.log4j.Logger
import uk.ac.sanger.scgcf.jira.services.models.*

/**
 * The {@code LabwareConverter} class represents a converter that converts a
 * JSON-API based json document to the appropriate object(s).
 * This converter created a {@code Labware} object and its relations, what was
 * represented in the json document.
 *
 * @author ke4
 *
 */
class LabwareConverter {

    static ResourceConverter labwareConverter
    static {
        JsonObjectMapper labwareMapper = new JsonObjectMapper()
        // TODO register only JavaTimeModule
//        materialMapper.registerModule(new JavaTimeModule())
        labwareMapper.findAndRegisterModules()
        labwareMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        labwareMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
        labwareConverter =
            new ResourceConverter(labwareMapper, Labware.class, LabwareType.class,
                Layout.class, Location.class, Metadatum.class, Receptacle.class)
    }

    /**
     * Convert a JSON string into a {@code Labware} instance
     * @param labwareJson The JSON string to be converted
     * @return The generated object
     */
    static Labware convertJsonToObject(String labwareJson) {
        labwareConverter.readObject(labwareJson.getBytes(), Labware.class)
    }

    /**
     * Convert a JSON string into a list of {@code Labware} instances
     * @param labwareJson The JSON string to be converted
     * @return The generated objects
     */
    static List<Labware> convertJsonToObjectCollection(String labwareJson) {
        labwareConverter.readObjectCollection(labwareJson.getBytes(), Labware.class)
    }

    /**
     * Converts a {@code Labware} into a JSON map for POSTing to the server
     * @param labware The object to be converted
     * @return The generated JSON map
     */
    @JsonCreator
    static def convertObjectToJson(labware) {
        [
            data: [
                id: labware.id,
                attributes: [
                    barcode: labware.barcode,
                    barcode_info: labware.barcodeInfo,
                    barcode_prefix: labware.barcodePrefix,
                    external_id: labware.externalId
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: labware.labwareType.name
                            ]
                        ]
                    ],
                    receptacles: [
                        data: labware.receptacles.collect { receptacle ->
                            [
                                attributes: [
                                    material_uuid: receptacle.materialUuid
                                ],
                                relationships: [
                                    location: [
                                        data: [
                                            attributes: [
                                                name: receptacle.location.name
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        }
                    ],
                    metadata: [
                        data: labware.metadata.collect {
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
    }
}
