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
class GetPlateByBarcodeTest extends Specification {

    def "getting the source plate with materials"() {
        setup:
        def sourcePlateBarcode = "SCGC-TST-00000006"
        def searchPlateByBarcodePath = RestServiceConfig.getLabwarePath()

        def restFunctionStub = new StubFor(RestService)
        restFunctionStub.demand.get(searchPlateByBarcodePath, [barcode: sourcePlateBarcode]) { path, queryParams ->
            new File('./src/test/groovy/test_plate_with_materials.json').text
        }

        when:
        def sourcePlate
        restFunctionStub.use {
            sourcePlate = LabwareActions.getPlateByBarcode(sourcePlateBarcode)
        } 

        then:
        sourcePlate.barcode == sourcePlateBarcode
    }
}
