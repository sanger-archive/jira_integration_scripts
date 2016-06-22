/**
 * See README.md for copyright details
 */
package actions

import exceptions.TransferException
import models.*
import spock.lang.Specification

import actions.TransferActions
import actions.MaterialActions
import actions.LabwareActions

/**
 * A test class for pool transfer action.
 *
 * @author ke4
 *
 */

class PoolActionTest extends Specification {

    def "there should be at least one source well"() {
        given:
        def sourceLabwareType = new LabwareType(name: "source type", layout: new Layout(name: "source layout", locations: []))
        def materialType = new MaterialType(name: 'new type')
        def pool = ['A1', 'A2', 'B1']

        def sourceLabware = new Labware(labwareType: new LabwareType(
            name: 'test_type', layout: new Layout(name: 'two plate layout')),
            barcode: 'TEST_001')
        def destinationLabware = new Labware(labwareType: new LabwareType(
            name: 'tube', layout: new Layout(name: 'tube')),
            receptacles: [
                new Receptacle(location: new Location(name: 'A1'))],
            barcode: 'TEST_001')

        when:
        TransferActions.pool(sourceLabware, destinationLabware, materialType, pool)

        then:
        TransferException ex = thrown()
        ex.message == "Source labware should contains at least 1 well"
    }

    def "the destination labware should be a tube"() {
        given:
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'B1')]
        def sourceLabwareType = new LabwareType(name: "source type", layout: new Layout(name: "source layout", locations: [
            locations[0],
            locations[1],
            locations[2]
        ]))
        def materialType = new MaterialType(name: 'new type')
        def pool = ['A1', 'A2', 'B1']

        def sourceLabware = new Labware(labwareType: new LabwareType(
            name: 'test_type', layout: new Layout(name: 'two plate layout')),
            receptacles: [
                new Receptacle(location: locations[0]),
                new Receptacle(location: locations[1]),
                new Receptacle(location: locations[2])
            ],
            barcode: 'TEST_001')
        def destinationLabware = new Labware(labwareType: new LabwareType(
            name: 'plate', layout: new Layout(name: 'plate')),
            receptacles: [
                new Receptacle(location: new Location(name: 'A1'))],
            barcode: 'TEST_001')

        when:
        TransferActions.pool(sourceLabware, destinationLabware, materialType, pool)

        then:
        TransferException ex = thrown()
        ex.message == "The destination labware should be a tube"
    }

    def "the destination labware should be empty"() {
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'B1')]
        def materialType = new MaterialType(name: 'new type')
        def pool = ['A1', 'A2', 'B1']

        def sourceLabware = new Labware(labwareType: new LabwareType(
            name: 'test_type', layout: new Layout(name: 'two plate layout')),
            receptacles: [
                new Receptacle(location: locations[0]),
                new Receptacle(location: locations[1]),
                new Receptacle(location: locations[2])
            ],
            barcode: 'TEST_001')
        def destinationLabware = new Labware(labwareType: new LabwareType(
            name: 'tube', layout: new Layout(name: 'tube')),
            receptacles: [
                new Receptacle(materialUuid: '123', location: new Location(name: 'A1'))],
            barcode: 'TEST_001')

        when:
        TransferActions.pool(sourceLabware, destinationLabware, materialType, pool)

        then:
        TransferException ex = thrown()
        ex.message == "The destination labware should be empty"
    }

    def "after pooling the new material should not contains metadata"() {
        def materialType = new MaterialType(name: 'new type')
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'B1')]
        def pool = ['A1', 'A2', 'B1']

        def sourceLabware = new Labware(labwareType: new LabwareType(
            name: 'test_type', layout: new Layout(name: 'two plate layout')),
            receptacles: [
                new Receptacle(materialUuid: '123', location: locations[0]),
                new Receptacle(materialUuid: '456', location: locations[1]),
                new Receptacle(materialUuid: '789', location: locations[2])
            ],
            barcode: 'TEST_001')
        def sourceMaterials = sourceLabware.receptacles.collect {
            new Material(id: it.materialUuid, name: "${labware.barcode}_${it.location.name}",
                metadata: [
                    new Metadatum(key: "metadata_0", value: "metadata_value_0"),
                    new Metadatum(key: "metadata_1", value: "metadata_value_1"),
                    new Metadatum(key: "metadata_2", value: "metadata_value_2")
                ]
            )
        }

        def destinationLabware = new Labware(labwareType: new LabwareType(
            name: 'tube', layout: new Layout(name: 'tube')),
            receptacles: [
                new Receptacle(materialUuid: '123', location: new Location(name: 'A1'))],
            barcode: 'TEST_001')
        def newMaterials = []

        when:
        TransferActions.pool(sourceLabware, destinationLabware, materialType, pool)

        then:
        1 * MaterialActions.getMaterials(sourceMaterials*.id) >> sourceMaterials
        1 * MaterialActions.postMaterials(_) >> { materials ->
            newMaterials = materials[0].eachWithIndex { material, i ->
                material.id = ids[i]
            }
        }
        1 * LabwareActions.updateLabware(destinationLabware) >> destinationLabware

        newMaterials[0].metadata.size() == 0
    }

    def "pooling an empty well should add a warning on the tube"() {
        def materialType = new MaterialType(name: 'new type')
        def sourceMaterials = [
            new Material(id: '123'),
            new Material(id: '456'),
            new Material(id: '789')
        ]
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'B1')]
        def pool = ['A1', 'A2', 'B1']

        def sourceLabware = new Labware(labwareType: new LabwareType(
            name: 'test_type', layout: new Layout(name: 'two plate layout')),
            receptacles: [
                new Receptacle(materialUuid: '123', location: locations[0]),
                new Receptacle(location: locations[1]),
                new Receptacle(materialUuid: '789', location: locations[2])
            ],
            barcode: 'TEST_001')
        def destinationLabware = new Labware(labwareType: new LabwareType(
            name: 'tube', layout: new Layout(name: 'tube')),
            receptacles: [
                new Receptacle(materialUuid: '123', location: new Location(name: 'A1'))],
            barcode: 'TEST_001')
        def newMaterials = []

        when:
        TransferActions.pool(sourceLabware, destinationLabware, materialType, pool)

        then:
        1 * MaterialActions.getMaterials(sourceMaterials*.id) >> sourceMaterials
        1 * MaterialActions.postMaterials(_) >> { materials ->
            newMaterials = materials[0].eachWithIndex { material, i ->
                material.id = ids[i]
            }
        }
        1 * LabwareActions.updateLabware(destinationLabware) >> destinationLabware

        destinationLabware.warnings.size() > 1
        destinationLabware.warnings[0] == 'The listed location(s) was empty in the source labware: A2'
    }

    def "it should pool four materials into one tube"() {
        def materialType = new MaterialType(name: 'new type')
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'),
            new Location(name: 'B1'), new Location(name: 'B2')]
        def pool = ['A1', 'A2', 'B1', 'B2']

        def sourceLabware = new Labware(labwareType: new LabwareType(
            name: 'test_type', layout: new Layout(name: 'two plate layout')),
            receptacles: [
                new Receptacle(materialUuid: '12', location: locations[0]),
                new Receptacle(materialUuid: '34', location: locations[1]),
                new Receptacle(materialUuid: '56', location: locations[2]),
                new Receptacle(materialUuid: '78', location: locations[3])
            ],
            barcode: 'TEST_001')
        def sourceMaterials = sourceLabware.receptacles.collect {
            new Material(id: it.materialUuid, name: "${labware.barcode}_${it.location.name}", metadata: [
                new Metadatum(key: "metadata_0", value: "metadata_value_0"),
                new Metadatum(key: "metadata_1", value: "metadata_value_1"),
                new Metadatum(key: "metadata_2", value: "metadata_value_2")
            ])
        }
        def destinationLabware = new Labware(labwareType: new LabwareType(
            name: 'tube', layout: new Layout(name: 'tube')),
            receptacles: [
                new Receptacle(location: new Location(name: 'A1'))],
            barcode: 'TEST_001')
        def newMetadata = [
            new Metadatum(key: 'new_key11', value: "new_value11"),
            new Metadatum(key: 'new_key21', value: "new_value21")
        ]

        def newMaterials = []
        GroovyMock(MaterialActions, global: true)
        GroovyMock(LabwareActions, global: true)

        when:
        TransferActions.pool(sourceLabware, destinationLabware, materialType, pool, newMetadata)

        then:
        1 * MaterialActions.getMaterials(sourceMaterials[3]*.id) >> sourceMaterials[3]
        1 * MaterialActions.postMaterials(_) >> { materials ->
            newMaterials += materials[0].each { material ->
                material.id = "${material.name}_uuid"
            }
            materials[0]
        }
        1 * LabwareActions.updateLabware(destinationLabware) >> destinationLabware

        newMaterials.size() == 1
        destinationLabware.materialUuids().size() == 1
        destinationLabware.materialUuids()[0] == newMaterials[0].id
        newMaterials[0].parents*.id == [ '12', '34', '56', '78']
        newMaterials[0].metadata as Set == newMetadata as Set
    }
}
