/**
 * See README.md for copyright details
 */
package actions

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
                                id: "2ea33500-fa6b-0133-af02-005056bf12f5"
                            ],
                            [
                                id: "c4b45610-f8d8-0133-2ac6-005056bf12f5"
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
}
