/**
 * See README.md for copyright details
 */
package actions

import models.Labware
import models.LabwareType
import models.Location
import models.Receptacle
import spock.lang.Specification
import utils.RestService
import utils.RestServiceConfig

/**
 * A test class for labware creation.
 *
 * @author ke4
 *
 */
class LabwareActionsTest extends Specification {

    def "creating a new destination plate"() {
        setup:
        def testExternalId = 'EXT_0001'
        def testBarcode = 'SCGC-TST-00000006'

        def restServiceStub = Stub(RestService)
        restServiceStub.post(_, _) >> new File('./src/test/groovy/test_plate.json').text
        LabwareActions.restService = restServiceStub

        when:
        def targetPlate = LabwareActions.newLabware('generic 96 well plate', testExternalId, barcode: testBarcode)

        then:
        targetPlate.externalId == testExternalId
        targetPlate.labwareType.name == 'generic 96 well plate'
    }

    def "getting the source plate with materials"() {
        setup:
        def sourcePlateBarcode = "SCGC-TST-00000006"
        def searchPlateByBarcodePath = RestServiceConfig.getLabwarePath()

        def restServiceStub = Stub(RestService)
        restServiceStub.get(searchPlateByBarcodePath, [barcode: sourcePlateBarcode]) >> new File('./src/test/groovy/test_plate_with_materials.json').text
        LabwareActions.restService = restServiceStub

        when:
        def sourcePlate = LabwareActions.getLabwareByBarcode(sourcePlateBarcode)

        then:
        sourcePlate.barcode == sourcePlateBarcode
    }

    def "perisiting a labware"() {
        setup:
        def labwareId = '1'
        def labwareBarcode = 'TEST_123'
        def labwareExternalId = '123456'
        def labwareTypeName = 'two well plate'

        def labware = new Labware(id: labwareId, barcode: labwareBarcode, externalId: labwareExternalId, labwareType: new LabwareType(name: labwareTypeName))
        labware.receptacles = [new Receptacle(materialUuid: '123', location: new Location(name: 'A1')), new Receptacle(materialUuid: '456', location: new Location(name: 'A2'))]

        def labwarePayload = [
            data: [
                id: labwareId,
                attributes: [
                    barcode: labwareBarcode,
                    external_id: labwareExternalId
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: labwareTypeName
                            ]
                        ]
                    ],
                    receptacles: [
                        data: [
                            [
                                attributes: [
                                    material_uuid: '123'
                                ],
                                relationships: [
                                    location: [
                                        data: [
                                            attributes: [
                                                name: 'A1'
                                            ]
                                        ]
                                    ]
                                ]
                            ],
                            [
                                attributes: [
                                    material_uuid: '456'
                                ],
                                relationships: [
                                    location: [
                                        data: [
                                            attributes: [
                                                name: 'A2'
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]

        def restServiceStub = Stub(RestService)
        restServiceStub.put(RestServiceConfig.labwarePath + labwareId, labwarePayload) >> new File('./src/test/groovy/test_plate_with_updated_materials.json').text
        LabwareActions.restService = restServiceStub

        when:
        labware = LabwareActions.updateLabware(labware)

        then:
        labware.externalId == labwareExternalId
        labware.barcode == labwareBarcode
        labware.receptacles*.materialUuid as Set == ['123', '456'] as Set
    }
}
