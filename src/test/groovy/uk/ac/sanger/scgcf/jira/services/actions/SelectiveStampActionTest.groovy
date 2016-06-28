/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.actions

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.exceptions.TransferException
import uk.ac.sanger.scgcf.jira.services.models.*

/**
 * A test class for stamp transfer action.
 *
 * @author rf9
 *
 */

class SelectiveStampActionTest extends Specification {

    def "can't stamp between layouts"() {
        setup:
        def sourceLabware = new Labware(labwareType: new LabwareType(layout: new Layout(name: 'test1')))
        def destLabware = new Labware(labwareType: new LabwareType(layout: new Layout(name: 'test2')))
        def copyLocations = ['A1', 'A3']

        when:
        TransferActions.selectiveStamp(sourceLabware, destLabware, new MaterialType(name: 'test_type'), copyLocations)

        then:
        thrown TransferException
    }

    def "the destination locations should be empty on the destination labware"() {
        setup:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'A3')]
        def copyLocations = ['A1', 'A3']
        def materialType = new MaterialType(name: 'new type')

        def sourceLabware = new Labware(labwareType: labwareType, barcode: 'TEST_001',
            receptacles: [
                new Receptacle(materialUuid: '123', location: locations[0]),
                new Receptacle(materialUuid: '456', location: locations[1]),
                new Receptacle(materialUuid: '789', location: locations[2])
            ])
        def destinationLabware = new Labware(labwareType: labwareType, barcode: 'TEST_002',
            receptacles: [
                new Receptacle(location: locations[0], materialUuid: '9123'),
                new Receptacle(location: locations[1]),
                new Receptacle(location: locations[2], materialUuid: '9124')
            ])

        when:
        TransferActions.selectiveStamp(sourceLabware, destinationLabware, materialType, copyLocations)

        then:
        TransferException ex = thrown()
        ex.message == "The following locations already occupied in the destination labware: A1, A3"
    }

    def "stamping between two plates"() {
        setup:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2')]
        def sourceMaterials = [new Material(id: '123'), new Material(id: '456')]
        def materialType = new MaterialType(name: 'new type')

        def sourceLabware = new Labware(labwareType: labwareType, barcode: 'TEST_001',
            receptacles: [new Receptacle(materialUuid: '123', location: locations[0]), new Receptacle(materialUuid: '456', location: locations[1])])
        def destinationLabware = Spy(Labware, constructorArgs: [[labwareType: labwareType, barcode: 'TEST_002',
            receptacles: [new Receptacle(location: locations[0]), new Receptacle(location: locations[1])]]])

        def copyLocations = ['A1']

        def ids = ['789', '012']
        def newMaterials = []
        GroovySpy(Material, global: true)

        when:
        destinationLabware = TransferActions.selectiveStamp(sourceLabware, destinationLabware, materialType, copyLocations)

        then:
        1 * Material.getMaterials(['123']) >> [sourceMaterials[0]]
        1 * Material.postMaterials(_) >> { materials ->
            newMaterials = materials[0].eachWithIndex { material, i ->
                material.id = ids[i]
            }
        }
        1 * destinationLabware.update() >> destinationLabware

        destinationLabware.receptacles[0].materialUuid == '789'
        destinationLabware.receptacles[1].materialUuid == null
        newMaterials.size() == 1
        newMaterials[0].parents[0] == sourceMaterials[0]
        newMaterials[0].name == 'TEST_002_A1'
    }

    def "stamping with a partially filled source plate"() {
        setup:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'A3')]
        def sourceMaterials = [new Material(id: '123'), new Material(id: '456')]
        def materialType = new MaterialType(name: 'new type')

        def sourceLabware = new Labware(labwareType: labwareType, barcode: 'TEST_001',
            receptacles: [new Receptacle(materialUuid: '123', location: locations[0]), new Receptacle(location: locations[1]), new Receptacle(materialUuid: '456', location: locations[2])])
        def destinationLabware = Spy(Labware, constructorArgs: [[labwareType: labwareType, receptacles: locations.collect { new Receptacle(location: it) }, barcode: 'TEST_002']])

        def copyLocations = ['A2', 'A3']

        def ids = ['789', '012']
        def newMaterials = []
        GroovySpy(Material, global: true)

        when:
        destinationLabware = TransferActions.selectiveStamp(sourceLabware, destinationLabware, materialType, copyLocations)

        then:
        1 * Material.getMaterials(['456']) >> [sourceMaterials[1]]
        1 * Material.postMaterials(_) >> { materials ->
            newMaterials = materials[0].eachWithIndex { material, i ->
                material.id = ids[i]
            }
        }
        1 * destinationLabware.update() >> destinationLabware

        destinationLabware.receptacles[0].materialUuid == null
        destinationLabware.receptacles[1].materialUuid == null
        destinationLabware.receptacles[2].materialUuid == '789'
        newMaterials.size() == 1
        newMaterials[0].parents[0] == sourceMaterials[1]
        newMaterials[0].name == 'TEST_002_A3'
    }

    def "stamping with metadata"() {
        setup:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2')]
        def sourceMaterials = [
            new Material(id: '123', metadata: [new Metadatum(key: "key1", value: "value1_1"), new Metadatum(key: "key2", value: "value2_1"), new Metadatum(key: "key3", value: "value3_1")]),
            new Material(id: '456', metadata: [new Metadatum(key: "key1", value: "value1_2"), new Metadatum(key: "key2", value: "value2_2"), new Metadatum(key: "key3", value: "value3_2")])
        ]
        def materialType = new MaterialType(name: 'new type')

        def sourceLabware = new Labware(labwareType: labwareType, barcode: 'TEST_001',
            receptacles: [new Receptacle(materialUuid: '123', location: locations[0]), new Receptacle(materialUuid: '456', location: locations[1])])
        def destinationLabware = Spy(Labware, constructorArgs: [[labwareType: labwareType, barcode: 'TEST_002',
            receptacles: [new Receptacle(location: locations[0]), new Receptacle(location: locations[1])]]])

        def copyLocations = ['A1']

        def ids = ['789', '012']
        def newMaterials = []
        GroovySpy(Material, global: true)

        when:
        destinationLabware = TransferActions.selectiveStamp(sourceLabware, destinationLabware, materialType, copyLocations, ["key1", "key3"])

        then:
        1 * destinationLabware.update() >> destinationLabware
        1 * Material.getMaterials([sourceMaterials[0].id]) >> [sourceMaterials[0]]
        1 * Material.postMaterials(_) >> { materials ->
            newMaterials = materials[0].eachWithIndex { material, i ->
                material.id = ids[i]
            }
        }

        destinationLabware.receptacles[0].materialUuid == '789'
        destinationLabware.receptacles[1].materialUuid == null
        newMaterials.size() == 1
        newMaterials[0].parents[0] == sourceMaterials[0]
        newMaterials[0].name == 'TEST_002_A1'

        newMaterials[0].metadata.size() == 2
        newMaterials[0].metadata[0].key == 'key1'
        newMaterials[0].metadata[0].value == 'value1_1'
        newMaterials[0].metadata[1].key == 'key3'
        newMaterials[0].metadata[1].value == 'value3_1'
    }

    def "stamping with additional metadata"() {
        setup:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'A3')]
        def sourceMaterials = [
            new Material(id: '123', metadata: [new Metadatum(key: "key1", value: "value1_1"), new Metadatum(key: "key2", value: "value2_1"), new Metadatum(key: "key3", value: "value3_1")]),
            new Material(id: '456', metadata: [new Metadatum(key: "key1", value: "value1_2"), new Metadatum(key: "key2", value: "value2_2"), new Metadatum(key: "key3", value: "value3_2")]),
            new Material(id: '789', metadata: [new Metadatum(key: "key1", value: "value1_3"), new Metadatum(key: "key2", value: "value2_3"), new Metadatum(key: "key3", value: "value3_3")])
        ]
        def newMetadata = [
            'A1': [
                new Metadatum(key: 'new_key11', value: "new_value11"),
                new Metadatum(key: 'new_key21', value: "new_value21")
            ],
            'A3': [
                new Metadatum(key: 'new_key13', value: "new_value13"),
                new Metadatum(key: 'new_key23', value: "new_value23")
            ]
        ]
        def materialType = new MaterialType(name: 'new type')

        def sourceLabware = new Labware(labwareType: labwareType, barcode: 'TEST_001',
            receptacles: [
                new Receptacle(materialUuid: '123', location: locations[0]),
                new Receptacle(materialUuid: '456', location: locations[1]),
                new Receptacle(materialUuid: '789', location: locations[2])
            ])
        def destinationLabware = Spy(Labware, constructorArgs: [[labwareType: labwareType, barcode: 'TEST_002',
            receptacles: [
                new Receptacle(location: locations[0]),
                new Receptacle(location: locations[1]),
                new Receptacle(location: locations[2])
            ]
        ]])

        def copyLocations = ['A1', 'A3']

        def ids = ['789', '012']
        def newMaterials = []
        GroovySpy(Material, global: true)

        when:
        destinationLabware = TransferActions.selectiveStamp(sourceLabware, destinationLabware,
            materialType, copyLocations, ["key1", "key3"], newMetadata)

        then:
        1 * destinationLabware.update() >> destinationLabware
        1 * Material.getMaterials([sourceMaterials[0].id, sourceMaterials[2].id]) >> [sourceMaterials[0], sourceMaterials[2]]
        1 * Material.postMaterials(_) >> { materials ->
            newMaterials = materials[0].eachWithIndex { material, i ->
                material.id = ids[i]
            }
        }

        destinationLabware.receptacles[0].materialUuid == '789'
        destinationLabware.receptacles[1].materialUuid == null
        destinationLabware.receptacles[2].materialUuid == '012'
        newMaterials.size() == 2
        newMaterials[0].parents[0] == sourceMaterials[0]
        newMaterials[0].name == 'TEST_002_A1'
        newMaterials[1].parents[0] == sourceMaterials[2]
        newMaterials[1].name == 'TEST_002_A3'

        newMaterials[0].metadata.size() == 4
        newMaterials[0].metadata[0].key == 'key1'
        newMaterials[0].metadata[0].value == 'value1_1'
        newMaterials[0].metadata[1].key == 'key3'
        newMaterials[0].metadata[1].value == 'value3_1'
        newMaterials[0].metadata[2].key == 'new_key11'
        newMaterials[0].metadata[2].value == 'new_value11'
        newMaterials[0].metadata[3].key == 'new_key21'
        newMaterials[0].metadata[3].value == 'new_value21'
        newMaterials[1].metadata.size() == 4
        newMaterials[1].metadata[0].key == 'key1'
        newMaterials[1].metadata[0].value == 'value1_3'
        newMaterials[1].metadata[1].key == 'key3'
        newMaterials[1].metadata[1].value == 'value3_3'
        newMaterials[1].metadata[2].key == 'new_key13'
        newMaterials[1].metadata[2].value == 'new_value13'
        newMaterials[1].metadata[3].key == 'new_key23'
        newMaterials[1].metadata[3].value == 'new_value23'
    }
}
