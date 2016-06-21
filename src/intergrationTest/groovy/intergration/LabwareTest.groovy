package intergration

import actions.LabwareActions
import exceptions.RestServiceException
import spock.lang.Specification

/**
 * Created by rf9 on 16/06/2016.
 */
class LabwareTest extends Specification {
    def "should be able to create an empty plate"() {
        given:
        def externalId = ((int) (Math.random() * 1000000000)).toString()
        def labwareTypeName = 'generic 96 well plate'

        when:
        def labware = LabwareActions.newLabware(labwareTypeName, externalId, barcode_prefix: 'TEST', barcode_info: 'XYZ')

        then:
        labware.id != null
        labware.externalId == externalId
        labware.barcode.startsWith('TEST-XYZ-')
        labware.labwareType.name == labwareTypeName
        labware.receptacles.size() == 96

        def locationNames = labware.receptacles*.location.name
        locationNames.unique().size() == 96
        locationNames as Set == ('A'..'H').collect { letter -> (1..12).collect { number -> "$letter$number" } }.flatten() as Set
        labware.receptacles*.materialUuid == (1..96).collect { null }

        def newLabware = LabwareActions.getLabwareByBarcode(labware.barcode)
        newLabware.id == labware.id
        newLabware.labwareType.name == labware.labwareType.name
        newLabware.receptacles*.location.name == labware.receptacles*.location.name
        newLabware.receptacles*.materialUuid == labware.receptacles*.materialUuid
    }

    def "should have unique barcodes"() {
        given:
        def labwareTypeName = 'generic 96 well plate'
        def firstLabware = LabwareActions.newLabware(labwareTypeName, ((int) (Math.random() * 1000000000)).toString(), barcode_prefix: 'TEST', barcode_info: 'XYZ')

        when:
        LabwareActions.newLabware(labwareTypeName, ((int) (Math.random() * 1000000000)).toString(), barcode: firstLabware.barcode)

        then:
        thrown RestServiceException
    }

    def "should have unique externalIds"() {
        given:
        def labwareTypeName = 'generic 96 well plate'
        def externalId = ((int) (Math.random() * 1000000000)).toString()
        def firstLabware = LabwareActions.newLabware(labwareTypeName, externalId, barcode_prefix: 'TEST', barcode_info: 'XYZ')

        when:
        LabwareActions.newLabware(labwareTypeName, externalId, barcode_prefix: 'TEST', barcode_info: 'XYZ')

        then:
        thrown RestServiceException
    }
}
