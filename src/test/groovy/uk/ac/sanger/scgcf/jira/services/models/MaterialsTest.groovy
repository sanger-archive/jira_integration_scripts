/**
 * See README.md for copyright details
 */
package uk.ac.sanger.scgcf.jira.services.models

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.utils.RestService

/**
 * A test class for labware creation.
 *
 * @author ke4
 *
 */
class MaterialsTest extends Specification {

    def "creating a new material"() {
        setup:
        def materialName = "Material name_1"
        def materialType = 'sample'

        def materialPayload = [
            data:[
                relationships: [
                    materials: [
                        data: [
                            [
                                id : null,
                                attributes: [
                                    name: materialName
                                ],
                                relationships: [
                                    material_type: [
                                        data: [
                                            attributes: [
                                                name: materialType
                                            ]
                                        ]
                                    ],
                                    metadata: [
                                        data: []
                                    ],
                                    parents: [
                                        data: []
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]

        def restServiceStub = Stub(RestService)
        restServiceStub.post(_, materialPayload) >> new File('./src/test/groovy/resources/test_material.json').text
        Material.restService = restServiceStub

        when:
        def newMaterial = Material.create(materialName, materialType)

        then:
        newMaterial.name == materialName
        newMaterial.id != null
        newMaterial.materialType.name == materialType
        newMaterial.metadata.size() == 0
    }

    def "creating a new material with metadata"() {
        setup:
        def materialName = "Material name_1"
        def materialType = 'sample'
        def metadata = [
            new Metadatum(key: 'metadata_1', value: 'metadata_1_value'),
            new Metadatum(key: 'metadata_2', value: 'metadata_2_value'),
            new Metadatum(key: 'metadata_3', value: 'metadata_3_value')
        ]

        def materialPayload = [
            data:[
                relationships: [
                    materials: [
                        data: [
                            [
                                id : null,
                                attributes: [
                                    name: materialName
                                ],
                                relationships: [
                                    material_type: [
                                        data: [
                                            attributes: [
                                                name: materialType
                                            ]
                                        ]
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
                                    ],
                                    parents: [
                                        data: []
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]

        def restServiceStub = Stub(RestService)
        restServiceStub.post(_, materialPayload) >> new File('./src/test/groovy/resources/test_material_with_metadata.json').text
        Material.restService = restServiceStub

        when:
        def newMaterial = Material.create(materialName, materialType, metadata)

        then:
        newMaterial.name == materialName
        newMaterial.id != null
        newMaterial.materialType.name == materialType
        newMaterial.metadata.size() == 3
        newMaterial.metadata == metadata
    }

    def "getting the material data from uuids"() {
        setup:
        def materialUuids = ["2ea33500-fa6b-0133-af02-005056bf12f5",
            "c4b45610-f8d8-0133-2ac6-005056bf12f5"]
        def restServiceStub = Stub(RestService)
        def materialUuidsPayload = [
            data: [
                relationships: [
                    materials: [
                        data: [
                            [
                                id: "2ea33500-fa6b-0133-af02-005056bf12f5",
                                attributes: [:],
                                relationships: [
                                    material_type: [:],
                                    metadata: [data: []],
                                    parents: [data: []]
                                ]
                            ],
                            [
                                id: "c4b45610-f8d8-0133-2ac6-005056bf12f5",
                                attributes: [:],
                                relationships: [
                                    material_type: [:],
                                    metadata: [data: []],
                                    parents: [data: []]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
        restServiceStub.post(_, materialUuidsPayload) >> new File('./src/test/groovy/resources/material_batch.json').text
        Material.restService = restServiceStub

        when:
        def materials = Material.getMaterials(materialUuids)

        then:
        materials.collect { it.id } as Set == materialUuids as Set
        materials[0].materialType.name == 'sample'
    }

    def "updating the materials"() {
        setup:
        def materials = [new Material(id: '123', name: 'test_name')]
        def materialPayload = [
            data: [
                relationships: [
                    materials: [
                        data: [
                            [
                                id: materials[0].id,
                                attributes: [
                                    name: materials[0].name
                                ],
                                relationships: [
                                    material_type: [:],
                                    metadata: [data: []],
                                    parents: [data: []]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]

        def restServiceStub = Stub(RestService)
        restServiceStub.post(_, materialPayload) >> new File('./src/test/groovy/resources/material_batch_post.json').text
        Material.restService = restServiceStub

        when:
        def new_materials = Material.postMaterials(materials);

        then:
        new_materials.size() == 1
        new_materials[0].name == 'test_name'
    }
}
