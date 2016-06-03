/**
 * See README.md for copyright details
 */
package actions

import models.*
import spock.lang.Specification

/**
 * A test class for labware creation.
 * 
 * @author ke4
 *
 */
class GetMaterialUUIDsFromPlateTest extends Specification {

    def "getting the material UUIDs from the plate"() {
        setup:
        def labware = new Labware()
        def receptacle1 = new Receptacle()
        receptacle1.material_uuid = "2ea33500-fa6b-0133-af02-005056bf12f5"
        def receptacle2 = new Receptacle()
        receptacle2.material_uuid = "2ea33500-fa6b-0133-af02-005056bf12f6"
        labware.receptacles = [receptacle1, receptacle2]

        when:
        def material_uuids = labware.materialUuids()

        then:
        material_uuids == [receptacle1.material_uuid, receptacle2.material_uuid]
    }
}
