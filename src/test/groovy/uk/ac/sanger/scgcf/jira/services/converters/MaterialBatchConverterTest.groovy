/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.converters

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.models.Material
import uk.ac.sanger.scgcf.jira.services.models.MaterialBatch
import uk.ac.sanger.scgcf.jira.services.models.MaterialType
import uk.ac.sanger.scgcf.jira.services.models.Metadatum

/**
 * Test class for MaterialBatch serialization
 *
 * @author rf9
 *
 */
class MaterialBatchConverterTest extends Specification {

    def "converting a MaterialBatch to json"() {
        setup:
        def materialBatch = new MaterialBatch(materials: [
            new Material(id: '1234', name: 'test_material_1', materialType: new MaterialType(name: 'sample'), metadata: [new Metadatum(key: 'key_1', value: 'value_1')], parents: [new Material(id: '123')]),
            new Material(id: '5678', name: 'test_material_2', materialType: new MaterialType(name: 'sample'), metadata: [new Metadatum(key: 'key_2', value: 'value_2')], parents: [new Material(id: '456')])
        ])

        when:
        def serializedJson = MaterialBatchConverter.convertObjectToJson(materialBatch)

        then:
        serializedJson.data.relationships.materials.data.size() == 2
        serializedJson.data.relationships.materials.data[0].id == '1234'
        serializedJson.data.relationships.materials.data[1].id == '5678'
        serializedJson.data.relationships.materials.data[0].attributes.name == 'test_material_1'
        serializedJson.data.relationships.materials.data[0].relationships.material_type.data.attributes.name == 'sample'
        serializedJson.data.relationships.materials.data[0].relationships.metadata.data[0].attributes.key == 'key_1'
        serializedJson.data.relationships.materials.data[0].relationships.metadata.data[0].attributes.value == 'value_1'
        serializedJson.data.relationships.materials.data[0].relationships.parents.data[0].id == '123'
    }
}
