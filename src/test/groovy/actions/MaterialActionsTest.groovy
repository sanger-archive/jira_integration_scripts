/**
 * See README.md for copyright details
 */
package actions

import models.Material
import spock.lang.Specification
import utils.RestService

/**
 * A test class for labware creation.
 *
 * @author ke4
 *
 */
class MaterialActionsTest extends Specification {

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
        restServiceStub.post(_, materialUuidsPayload) >> new File('./src/test/groovy/material_batch.json').text
        MaterialActions.restService = restServiceStub

        when:
        def materials = MaterialActions.getMaterials(materialUuids)

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
                                id: '123',
                                attributes: [
                                    name: 'test_name'
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
        restServiceStub.post(_, materialPayload) >> new File('./src/test/groovy/material_batch_post.json').text
        MaterialActions.restService = restServiceStub

        when:
        def new_materials = MaterialActions.postMaterials(materials);

        then:
        new_materials.size() == 1
        new_materials[0].name == 'test_name'
    }
}
