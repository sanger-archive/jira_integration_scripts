/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.actions

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.exceptions.TransferException
import uk.ac.sanger.scgcf.jira.services.models.*

/**
 * A test class for split transfer action.
 *
 * @author ke4
 *
 */

class CombineActionTest extends Specification {

    def "there should be at least one source plate"() {
        given:
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'A3')]
        def materialType = new MaterialType(name: 'new type')

        def destinationLabware = new Labware(labwareType: new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout')),
            receptacles: [
                new Receptacle(location: locations[0]),
                new Receptacle(location: locations[1]),
                new Receptacle(location: locations[2])
            ], barcode: 'TEST_001')

        when:
        TransferActions.combine([], destinationLabware, materialType)

        then:
        TransferException ex = thrown()
        ex.message == "Must supply at least one source plate"
    }

    def "there should be no more than four source plates"() {
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'A3')]
        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = (1..5).collect { new Labware(labwareType: labwareType) }
        def destinationLabware = new Labware(labwareType: labwareType,
            receptacles: [
                new Receptacle(location: locations[0]),
                new Receptacle(location: locations[1]),
                new Receptacle(location: locations[2])
            ], barcode: 'TEST_001')

        when:
        TransferActions.combine(sourceLabwares, destinationLabware, materialType)

        then:
        TransferException ex = thrown()
        ex.message == "Must supply at most four source plates"
    }

    def "all the source plates should have the same layout"() {
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'two plate layout'))
        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = (1..4).collect { new Labware(labwareType: new LabwareType(layout: new Layout(name: " Labware Type $it"))) }
        def destinationLabware = new Labware(labwareType: labwareType,
            receptacles: [], barcode: 'TEST_001')

        when:
        TransferActions.combine(sourceLabwares, destinationLabware, materialType)

        then:
        TransferException ex = thrown()
        ex.message == "All source plates must have the same layout"
    }

    def "the destination plate should have four times the receptacles of the source plate"() {
        def sourceLabwareType = new LabwareType(name: "source type", layout: new Layout(name: "source layout", locations: [
            new Location(name: 'A1'),
            new Location(name: 'A2'),
            new Location(name: 'B1'),
            new Location(name: 'B2')
        ]))

        def destinationLabwareType = new LabwareType(name: "destination type", layout: new Layout(name: "destination layout", locations: [
            new Location(name: 'A1'),
            new Location(name: 'A2'),
            new Location(name: 'B1'),
            new Location(name: 'B2')
        ]))

        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = (1..4).collect { new Labware(labwareType: sourceLabwareType) }
        def destinationLabware = new Labware(labwareType: destinationLabwareType, barcode: 'TEST_001')

        when:
        TransferActions.combine(sourceLabwares, destinationLabware, materialType)

        then:
        TransferException ex = thrown()
        ex.message == "The destination layout should be four times the source layout"
    }

    def "the destination plate should be empty"() {
        def sourceLabwareType = new LabwareType(name: "source type", layout: new Layout(name: "source layout",
            locations: ('A'..'B').collect { letter -> (1..2).collect { number -> new Location(name: "$letter$number") } }.flatten()
        ))

        def destinationLabwareType = new LabwareType(name: "destination type", layout: new Layout(name: "destination layout",
            locations: ('A'..'D').collect { letter -> (1..4).collect { number -> new Location(name: "$letter$number") } }.flatten()
        ))

        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = (1..4).collect { new Labware(labwareType: sourceLabwareType) }
        def destinationLabware = new Labware(labwareType: destinationLabwareType, barcode: 'TEST_001',
            receptacles: [new Receptacle(materialUuid: '123')]
        )

        when:
        TransferActions.combine(sourceLabwares, destinationLabware, materialType)

        then:
        TransferException ex = thrown()
        ex.message == "The destination plate should be empty"
    }

    def "it should combine four plates into one"() {
        def sourceLabwareType = new LabwareType(name: "source type", layout: new Layout(name: "source layout", row: 2, column: 2,
            locations: ('A'..'B').collect { letter -> (1..2).collect { number -> new Location(name: "$letter$number") } }.flatten()
        ))
        def destinationLabwareType = new LabwareType(name: "destination type", layout: new Layout(name: "destination layout", row: 4, column: 4,
            locations: ('A'..'D').collect { letter -> (1..4).collect { number -> new Location(name: "$letter$number") } }.flatten()
        ))
        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = (1..4).collect { labwareCount ->
            new Labware(labwareType: sourceLabwareType, barcode: "TEST_00$labwareCount",
                receptacles: sourceLabwareType.layout.locations.collect { new Receptacle(location: it, materialUuid: "${labwareCount}_${it.name}_uuid") }
            )
        }
        def sourceMaterials = sourceLabwares.collect { labware ->
            labware.receptacles.collect {
                new Material(id: it.materialUuid, name: "${labware.barcode}_${it.location.name}", metadata: [
                    new Metadatum(key: "metadata_0", value: "metadata_value_0"),
                    new Metadatum(key: "metadata_1", value: "metadata_value_1"),
                    new Metadatum(key: "metadata_2", value: "metadata_value_2")
                ])
            }
        }
        def destinationLabware = new Labware(labwareType: destinationLabwareType, barcode: 'TEST_010',
            receptacles: destinationLabwareType.layout.locations.collect { new Receptacle(location: it) }
        )
        def additionalMetadata = destinationLabware.receptacles.collectEntries { receptacle ->
            [receptacle.location.name, [new Metadatum(key: 'new_metadata', value: receptacle.location.name)]]
        }
        def newMaterials = []
        GroovyMock(MaterialActions, global: true)
        GroovySpy(TransferActions, global: true)

        when:
        destinationLabware = TransferActions.combine(sourceLabwares, destinationLabware, materialType, ["metadata_0", "metadata_2"], additionalMetadata)

        then:
        1 * MaterialActions.getMaterials(sourceMaterials[0]*.id) >> sourceMaterials[0]
        1 * MaterialActions.getMaterials(sourceMaterials[1]*.id) >> sourceMaterials[1]
        1 * MaterialActions.getMaterials(sourceMaterials[2]*.id) >> sourceMaterials[2]
        1 * MaterialActions.getMaterials(sourceMaterials[3]*.id) >> sourceMaterials[3]
        4 * MaterialActions.postMaterials(_) >> { materials ->
            newMaterials += materials[0].each { material ->
                material.id = "${material.name}_uuid"
            }
            materials[0]
        }
        1 * TransferActions.updateLabware(destinationLabware) >> destinationLabware

        newMaterials.size() == 16
        destinationLabware.materialUuids().size() == 16
        def expectedParents = [
            '1_A1_uuid', '2_A1_uuid', '1_A2_uuid', '2_A2_uuid',
            '3_A1_uuid', '4_A1_uuid', '3_A2_uuid', '4_A2_uuid',
            '1_B1_uuid', '2_B1_uuid', '1_B2_uuid', '2_B2_uuid',
            '3_B1_uuid', '4_B1_uuid', '3_B2_uuid', '4_B2_uuid',
        ]
        destinationLabware.receptacles.eachWithIndex { receptacle, i ->
            def newMaterial = newMaterials.find { it.id == receptacle.materialUuid }
            assert newMaterial != null
            assert newMaterial.name == "${destinationLabware.barcode}_${receptacle.location.name}"
            assert newMaterial.parents[0].id == expectedParents[i]
        }
    }

    def "it should combine two plates into one with gaps"() {
        def sourceLabwareType = new LabwareType(name: "source type", layout: new Layout(name: "source layout", row:2, column: 2,
            locations: ('A'..'B').collect { letter -> (1..2).collect { number -> new Location(name: "$letter$number") } }.flatten()
        ))
        def destinationLabwareType = new LabwareType(name: "destination type", layout: new Layout(name: "destination layout", row: 4, column: 4,
            locations: ('A'..'D').collect { letter -> (1..4).collect { number -> new Location(name: "$letter$number") } }.flatten()
        ))
        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = (1..2).collect { labwareCount ->
            new Labware(labwareType: sourceLabwareType, barcode: "TEST_00$labwareCount",
                receptacles: sourceLabwareType.layout.locations.collect { new Receptacle(location: it, materialUuid: "${labwareCount}_${it.name}_uuid") }
            )
        }
        def sourceMaterials = sourceLabwares.collect { labware ->
            labware.receptacles.collect {
                new Material(id: it.materialUuid, name: "${labware.barcode}_${it.location.name}", metadata: [
                    new Metadatum(key: "metadata_0", value: "metadata_value_0"),
                    new Metadatum(key: "metadata_1", value: "metadata_value_1"),
                    new Metadatum(key: "metadata_2", value: "metadata_value_2")
                ])
            }
        }
        def destinationLabware = new Labware(labwareType: destinationLabwareType, barcode: 'TEST_010',
            receptacles: destinationLabwareType.layout.locations.collect { new Receptacle(location: it) }
        )
        def additionalMetadata = destinationLabware.receptacles.collectEntries { receptacle ->
            [receptacle.location.name, [new Metadatum(key: 'new_metadata', value: receptacle.location.name)]]
        }
        def newMaterials = []
        GroovyMock(MaterialActions, global: true)
        GroovySpy(TransferActions, global: true)

        when:
        destinationLabware = TransferActions.combine(sourceLabwares, destinationLabware, materialType, ["metadata_0", "metadata_2"], additionalMetadata)

        then:
        1 * MaterialActions.getMaterials(sourceMaterials[0]*.id) >> sourceMaterials[0]
        1 * MaterialActions.getMaterials(sourceMaterials[1]*.id) >> sourceMaterials[1]
        2 * MaterialActions.postMaterials(_) >> { materials ->
            newMaterials += materials[0].each { material ->
                material.id = "${material.name}_uuid"
            }
            materials[0]
        }
        1 * TransferActions.updateLabware(destinationLabware) >> destinationLabware

        newMaterials.size() == 8
        destinationLabware.materialUuids().size() == 8
        def expectedParents = [
            '1_A1_uuid', '2_A1_uuid', '1_A2_uuid', '2_A2_uuid',
            null, null, null, null,
            '1_B1_uuid', '2_B1_uuid', '1_B2_uuid', '2_B2_uuid',
            null, null, null, null,
        ]
        destinationLabware.receptacles.eachWithIndex { receptacle, i ->
            def newMaterial = newMaterials.find { it.id == receptacle.materialUuid }
            if (((int) (i / 4)) % 2 == 0) {
                assert newMaterial != null
                assert newMaterial.name == "${destinationLabware.barcode}_${receptacle.location.name}"
                assert newMaterial.parents[0].id == expectedParents[i]
            } else {
                assert newMaterial == null
            }
        }
    }
}
