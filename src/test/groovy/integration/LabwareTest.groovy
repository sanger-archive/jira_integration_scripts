package integration

import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.exceptions.RestServiceException
import uk.ac.sanger.scgcf.jira.services.models.Labware
import uk.ac.sanger.scgcf.jira.services.models.LabwareTypes

/**
 * Created by rf9 on 16/06/2016.
 */
class LabwareTest extends Specification {
    def "should be able to create an empty plate"() {
        given:
        def externalId = ((int) (Math.random() * 1000000000)).toString()
        def labwareType = LabwareTypes.GENERIC_96_PLATE

        when:
        def labware = Labware.create(labwareType, externalId, barcode_prefix: 'TEST', barcode_info: 'XYZ')

        then:
        labware.id != null
        labware.externalId == externalId
        labware.barcode.startsWith('TEST-XYZ-')
        labware.labwareType == labwareType
        labware.receptacles.size() == 96

        def locationNames = labware.receptacles*.location.name
        locationNames.unique().size() == 96
        locationNames as Set == ('A'..'H').collect { letter -> (1..12).collect { number -> "$letter$number" } }.flatten() as Set
        labware.receptacles*.materialUuid == (1..96).collect { null }

        def newLabware = Labware.findByBarcode(labware.barcode)
        newLabware.id == labware.id
        newLabware.labwareType.name == labware.labwareType.name
        newLabware.receptacles*.location.name == labware.receptacles*.location.name
        newLabware.receptacles*.materialUuid == labware.receptacles*.materialUuid
    }

    def "should have unique barcodes"() {
        given:
        def labwareType = LabwareTypes.GENERIC_96_PLATE
        def firstLabware = Labware.create(labwareType, ((int) (Math.random() * 1000000000)).toString(), barcode_prefix: 'TEST', barcode_info: 'XYZ')

        when:
        Labware.create(labwareType, ((int) (Math.random() * 1000000000)).toString(), barcode: firstLabware.barcode)

        then:
        thrown RestServiceException
    }

    def "should have unique externalIds"() {
        given:
        def labwareType = LabwareTypes.GENERIC_96_PLATE
        def externalId = ((int) (Math.random() * 1000000000)).toString()
        Labware.create(labwareType, externalId, barcode_prefix: 'TEST', barcode_info: 'XYZ')

        when:
        Labware.create(labwareType, externalId, barcode_prefix: 'TEST', barcode_info: 'XYZ')

        then:
        thrown RestServiceException
    }

    def "should be able to find labware by barcode"() {
        given:
        def labware = Labware.create(LabwareTypes.GENERIC_96_PLATE, ((int) (Math.random() * 1000000000)).toString(), barcode_prefix: 'TEST')

        when:
        def foundLabware = Labware.findByBarcode(labware.barcode)

        then:
        foundLabware.barcode == labware.barcode
        foundLabware.externalId == labware.externalId
    }
}
