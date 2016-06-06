/**
 * See README.md for copyright details
 */
package actions

import models.Labware
import models.Receptacle
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
