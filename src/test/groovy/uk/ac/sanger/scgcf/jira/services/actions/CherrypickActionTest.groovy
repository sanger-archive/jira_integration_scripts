package uk.ac.sanger.scgcf.jira.services.actions

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.models.*

/**
 * Created by rf9 on 27/06/2016.
 */
class CherrypickActionTest extends Specification {

    def "should cherrypick from one plate to another"() {
        given:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'four plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'B1'), new Location(name: 'B2')]
        def sourceMaterials = (1..4).collect { new Material(id: "source_id_$it", name: "source_material_$it") }
        def materialType = new MaterialType(name: 'new type')

        def sourceLabware = new Labware(barcode: 'TEST_001', labwareType: labwareType, receptacles: (0..3).collect { new Receptacle(materialUuid: sourceMaterials[it].id, location: locations[it]) })
        def destinationLabware = new Labware(barcode: 'TEST_002', labwareType: labwareType, receptacles: locations.collect { new Receptacle(location: it) })

        def newMaterials = []
        GroovySpy(TransferActions, global: true)

        def transferMap = [
            new TransferMapping(sourceBarcode: 'TEST_001', destinationBarcode: 'TEST_002', sourceLocation: 'A1', destinationLocation: 'A2'),
            new TransferMapping(sourceBarcode: 'TEST_001', destinationBarcode: 'TEST_002', sourceLocation: 'B1', destinationLocation: 'B2')
        ]

        when:
        TransferActions.cherrypick([sourceLabware], [destinationLabware], materialType, transferMap)

        then:
        1 * TransferActions.getMaterialsByUuid([sourceMaterials[0].id, sourceMaterials[2].id]) >> [sourceMaterials[0], sourceMaterials[2]]
        1 * TransferActions.postNewMaterials(_) >> { materials ->
            newMaterials += materials[0].eachWithIndex { material, i ->
                material.id = "${material.name}_uuid"
            }
        }
        1 * TransferActions.updateLabware(destinationLabware) >> destinationLabware

        destinationLabware.receptacles[1].materialUuid == newMaterials[0].id
        destinationLabware.receptacles[3].materialUuid == newMaterials[1].id
        newMaterials[0].parents[0].id == sourceMaterials[0].id
        newMaterials[1].parents[0].id == sourceMaterials[2].id
    }

    def "should cherrypick many to many plates"() {
        given:
        def labwareType = new LabwareType(name: 'test_type', layout: new Layout(name: 'four plate layout'))
        def locations = [new Location(name: 'A1'), new Location(name: 'A2'), new Location(name: 'B1'), new Location(name: 'B2')]
        def sourceMaterials = (1..8).collect { new Material(id: "source_id_$it", name: "source_material_$it") }
        def materialType = new MaterialType(name: 'new type')

        def sourceLabwares = [
            new Labware(barcode: 'TEST_001', labwareType: labwareType, receptacles: (0..3).collect {
                new Receptacle(materialUuid: sourceMaterials[it].id, location: locations[it])
            }),
            new Labware(barcode: 'TEST_002', labwareType: labwareType, receptacles: (0..3).collect {
                new Receptacle(materialUuid: sourceMaterials[it + 4].id, location: locations[it])
            })
        ]
        def destinationLabwares = [
            new Labware(barcode: 'TEST_003', labwareType: labwareType, receptacles: locations.collect { new Receptacle(location: it) }),
            new Labware(barcode: 'TEST_004', labwareType: labwareType, receptacles: locations.collect { new Receptacle(location: it) })
        ]

        def newMaterials = []
        GroovySpy(TransferActions, global: true)

        def transferMap = [
            new TransferMapping(sourceBarcode: 'TEST_001', destinationBarcode: 'TEST_003', sourceLocation: 'A1', destinationLocation: 'A1'),
            new TransferMapping(sourceBarcode: 'TEST_001', destinationBarcode: 'TEST_004', sourceLocation: 'B1', destinationLocation: 'B1'),
            new TransferMapping(sourceBarcode: 'TEST_002', destinationBarcode: 'TEST_003', sourceLocation: 'A2', destinationLocation: 'A2'),
            new TransferMapping(sourceBarcode: 'TEST_002', destinationBarcode: 'TEST_004', sourceLocation: 'B2', destinationLocation: 'B2')
        ]

        when:
        TransferActions.cherrypick(sourceLabwares, destinationLabwares, materialType, transferMap)

        then:
        1 * TransferActions.getMaterialsByUuid([0, 2, 5, 7].collect { sourceMaterials[it].id }) >> [0, 2, 5, 7].collect { sourceMaterials[it] }
        1 * TransferActions.postNewMaterials(_) >> { materials ->
            newMaterials += materials[0].eachWithIndex { material, i ->
                material.id = "${material.name}_uuid"
            }
        }
        2 * TransferActions.updateLabware(_) >> { it[0] }

        destinationLabwares[0].receptacles[0].materialUuid == newMaterials[0].id
        destinationLabwares[1].receptacles[2].materialUuid == newMaterials[1].id
        destinationLabwares[0].receptacles[1].materialUuid == newMaterials[2].id
        destinationLabwares[1].receptacles[3].materialUuid == newMaterials[3].id

        newMaterials[0].parents[0].id == sourceMaterials[0].id
        newMaterials[1].parents[0].id == sourceMaterials[2].id
        newMaterials[2].parents[0].id == sourceMaterials[5].id
        newMaterials[3].parents[0].id == sourceMaterials[7].id
    }
}
