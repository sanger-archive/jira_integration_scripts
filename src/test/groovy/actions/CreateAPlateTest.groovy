/**
 * See README.md for copyright details
 */
package actions

import groovy.mock.interceptor.StubFor
import models.*
import spock.lang.Specification
import utils.RestService

/**
 * A test class for labware creation.
 * 
 * @author ke4
 *
 */
class CreateAPlateTest extends Specification {

    def "creating a new destination plate"() {
        setup:
        def testExternalId = 'EXT_0001'
        def testBarcode = 'SCGC-TST-00000006'

        def restFunctionStub = new StubFor(RestService)
        restFunctionStub.demand.post(any(), any()) { path, payload ->
            new File('./src/test/groovy/test_plate.json').text
        }

        when:
        def targetPlate
        restFunctionStub.use {
            targetPlate = LabwareActions.newPlate('generic 96 well plate', testExternalId, barcode: testBarcode)
        }

        then:
        targetPlate.external_id == testExternalId
        targetPlate.labware_type.name == 'generic 96 well plate'
    }
}
