/**
 * See README.md for copyright details
 */
package actions

import groovy.mock.interceptor.StubFor
import models.*
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
        def targetPlate = LabwareActions.newPlate('generic 96 well plate', testExternalId, barcode: testBarcode)

        then:
        targetPlate.external_id == testExternalId
        targetPlate.labware_type.name == 'generic 96 well plate'
    }

    def "getting the source plate with materials"() {
        setup:
        def sourcePlateBarcode = "SCGC-TST-00000006"
        def searchPlateByBarcodePath = RestServiceConfig.getLabwarePath()

        def restServiceStub = Stub(RestService)
        restServiceStub.get(searchPlateByBarcodePath, [ barcode: sourcePlateBarcode ]) >> new File('./src/test/groovy/test_plate_with_materials.json').text
        LabwareActions.restService = restServiceStub

        when:
        def sourcePlate = LabwareActions.getPlateByBarcode(sourcePlateBarcode)

        then:
        sourcePlate.barcode == sourcePlateBarcode
    }
}
