/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.jasminb.jsonapi.ResourceConverter
import uk.ac.sanger.scgcf.jira.services.models.Material
import uk.ac.sanger.scgcf.jira.services.models.MaterialBatch
import uk.ac.sanger.scgcf.jira.services.models.MaterialType
import uk.ac.sanger.scgcf.jira.services.models.Metadatum

/**
 * The {@code MaterialBatchConverter} class represents a converter that converts a
 * JSON-API based json document to the appropriate object(s).
 * This converter created a {@code MaterialBatch} object and its relations, what was
 * represented in the json document.
 *
 * @author ke4
 *
 */
class MaterialBatchConverter {

    static ResourceConverter materialBatchConverter
    static {
        ObjectMapper materialBatchMapper = new ObjectMapper()
        // TODO register only JavaTimeModule
//        materialMapper.registerModule(new JavaTimeModule())
        materialBatchMapper.findAndRegisterModules()
        materialBatchMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        materialBatchMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
        materialBatchConverter = new ResourceConverter(materialBatchMapper, MaterialBatch.class, Material.class, Metadatum.class, MaterialType.class)
    }

    /**
     * Convert a JSON string into a {@code MaterialBatch} instance
     * @param materialBatchJson The JSON string to be converted
     * @return The generated object
     */
    static MaterialBatch convertJsonToObject(String materialBatchJson) {
        materialBatchConverter.readObject(materialBatchJson.getBytes(), MaterialBatch.class)
    }

    /**
     * Converts a {@code MaterialBatch} into a JSON map for POSTing to the server
     * @param materialBatch The object to be converted
     * @return The generated JSON map
     */
    def static convertObjectToJson(MaterialBatch materialBatch) {
        [
            data: [
                relationships: [
                    materials: [
                        data: materialBatch.materials.collect { material ->
                            [
                                id: material.id,
                                attributes: material.name ? [
                                    name: material.name
                                ] : [:],
                                relationships: [
                                    material_type: material.materialType ? [
                                        data: [
                                            attributes: [
                                                name: material.materialType.name
                                            ]
                                        ]
                                    ] : [:],
                                    metadata: [
                                        data: material.metadata.collect { metadatum ->
                                            [
                                                attributes: [
                                                    key: metadatum.key,
                                                    value: metadatum.value
                                                ]
                                            ]
                                        }
                                    ],
                                    parents: [
                                        data: material.parents.collect { parent ->
                                            [
                                                id: parent.id
                                            ]
                                        }
                                    ]
                                ]
                            ]
                        }
                    ]
                ]
            ]
        ]
    }
}
