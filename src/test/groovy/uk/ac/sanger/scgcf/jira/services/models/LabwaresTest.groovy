/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.utils.RestService
import uk.ac.sanger.scgcf.jira.services.utils.RestServiceConfig

/**
 * A test class for labware creation.
 *
 * @author ke4
 *
 */
class LabwaresTest extends Specification {

    def "creating a new destination plate"() {
        setup:
        def testExternalId = 'EXT_0001'
        def testBarcode = 'SCGC-TST-00000006'

        def labwarePayload = [
            data: [
                id: null,
                attributes: [
                    barcode: testBarcode,
                    barcode_prefix: null,
                    barcode_info: null,
                    external_id: testExternalId
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: LabwareTypes.GENERIC_96_PLATE.name
                            ]
                        ]
                    ],
                    receptacles: [
                        data: []
                    ],
                    metadata: [
                        data: []
                    ]
                ]
            ]
        ]

        def restServiceStub = Stub(RestService)
        restServiceStub.post(_, labwarePayload) >> new File('./src/test/groovy/resources/test_plate.json').text
        Labware.restService = restServiceStub

        when:
        def targetPlate = Labware.create(LabwareTypes.GENERIC_96_PLATE, testExternalId, barcode: testBarcode)

        then:
        targetPlate.labwareType.name == LabwareTypes.GENERIC_96_PLATE.name
        targetPlate.externalId == testExternalId
    }

    def "creating a new destination plate with metadata"() {
        setup:
        def testExternalId = 'EXT_0001'
        def testBarcode = 'SCGC-TST-00000006'
        def metadata = [
            new Metadatum(key: 'metadata_1', value: 'metadata_1_value'),
            new Metadatum(key: 'metadata_2', value: 'metadata_2_value')
        ]

        def labwarePayload = [
            data: [
                id: null,
                attributes: [
                    barcode: testBarcode,
                    barcode_prefix: null,
                    barcode_info: null,
                    external_id: testExternalId
                ],
                relationships: [
                    labware_type: [
                        data: [
                            attributes: [
                                name: LabwareTypes.GENERIC_96_PLATE.name
                            ]
                        ]
                    ],
                    receptacles: [
                        data: []
                    ],
                    metadata: [
                        data: metadata.collect {
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

        def restServiceStub = Stub(RestService)
        restServiceStub.post(_, labwarePayload) >> new File('./src/test/groovy/resources/test_plate_with_metadata.json').text
        Labware.restService = restServiceStub

        when:
        def targetPlate = Labware.create(LabwareTypes.GENERIC_96_PLATE, testExternalId, metadata, barcode: testBarcode)

        then:
        targetPlate.labwareType.name == LabwareTypes.GENERIC_96_PLATE.name
        targetPlate.externalId == testExternalId
        targetPlate.metadata == metadata
    }

    def "getting the source plate with materials"() {
        setup:
        def sourcePlateBarcode = "SCGC-TST-00000006"
        def searchPlateByBarcodePath = RestServiceConfig.getLabwarePath()

        def restServiceStub = Stub(RestService)
        restServiceStub.get(searchPlateByBarcodePath, [barcode: sourcePlateBarcode]) >> new File('./src/test/groovy/resources/test_plate_with_materials.json').text
        Labware.restService = restServiceStub

        when:
        def sourcePlate = Labware.findByBarcode(sourcePlateBarcode)

        then:
        sourcePlate.barcode == sourcePlateBarcode
    }

    def "persisting a labware"() {
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
                    barcode_prefix: null,
                    barcode_info: null,
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
                        data: labware.receptacles.collect {
                            [
                                attributes: [
                                    material_uuid: it.materialUuid
                                ],
                                relationships: [
                                    location: [
                                        data: [
                                            attributes: [
                                                name: it.location.name
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

        def restServiceStub = Stub(RestService)
        restServiceStub.put(RestServiceConfig.labwarePath + labwareId, labwarePayload) >> new File('./src/test/groovy/resources/test_plate_with_updated_materials.json').text
        Labware.restService = restServiceStub

        when:
        labware.update()

        then:
        labware.externalId == labwareExternalId
        labware.barcode == labwareBarcode
        labware.receptacles*.materialUuid as Set == ['123', '456'] as Set
    }

    def "getting the material UUIDs from the plate"() {
        setup:
        def labware = new Labware()
        def receptacle1 = new Receptacle()
        receptacle1.materialUuid = "2ea33500-fa6b-0133-af02-005056bf12f5"
        def receptacle2 = new Receptacle()
        receptacle2.materialUuid = "2ea33500-fa6b-0133-af02-005056bf12f6"
        labware.receptacles = [receptacle1, receptacle2]

        when:
        def materialUuids = labware.materialUuids()

        then:
        materialUuids == [receptacle1.materialUuid, receptacle2.materialUuid]
    }
}
