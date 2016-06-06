/**
 * See README.md for copyright details
 */
package actions

import models.*
import spock.lang.Specification
import utils.RestService
import actions.MaterialActions

/**
 * A test class for labware creation.
 * 
 * @author ke4
 *
 */
class MaterialActionsTest extends Specification {

    def "getting the material data from uuids"() {
        setup:
        def material_uuids = ["2ea33500-fa6b-0133-af02-005056bf12f5",
            "c4b45610-f8d8-0133-2ac6-005056bf12f5"]
        def restServiceStub = Stub(RestService)
        def material_uuids_payload = [
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
        restServiceStub.post(_, material_uuids_payload) >> new File('./src/test/groovy/material_batch.json').text
        MaterialActions.restService = restServiceStub

        when:
        def materials = MaterialActions.getMaterials(material_uuids)

        then:
        def response_material_uuids = materials.collect { it.id }
        assert response_material_uuids as Set == material_uuids as Set
    }
}
