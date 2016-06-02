/**
 * See README.md for copyright details
 */
package actions

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.jasminb.jsonapi.ResourceConverter;

import groovy.json.JsonSlurper
import groovy.mock.interceptor.StubFor
import models.*;
import spock.lang.Specification

/**
 * A test class for labware creation.
 * 
 * @author ke4
 *
 */
class CreateAPlateTest extends Specification {

    def "creating a new destination plate"() {
        setup:
        def plateCreator = new LabwareFactory()
        def test_external_id = 'EXT_0001'
        plateCreator.setExternal_id(test_external_id)

        def restFunctionStub = new StubFor(utils.RestService)
        restFunctionStub.demand.post(any(), any()) { path, payload ->
            new File('./src/test/groovy/test_plate.json').text
        }

        when:
        def targetPlate
        restFunctionStub.use {
            targetPlate = plateCreator.newPlate(LabwareFactory.GENERIC_96_WELL_PLATE_TYPE)
        }

        then:
        targetPlate.external_id == test_external_id
        targetPlate.labware_type.name == LabwareFactory.GENERIC_96_WELL_PLATE_TYPE
    }
}
