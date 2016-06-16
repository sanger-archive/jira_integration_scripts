package intergration

import com.github.jasminb.jsonapi.ResourceConverter
import models.LabwareType
import models.Layout
import models.Location
import spock.lang.Specification
import utils.RestService
import utils.RestServiceConfig

/**
 * Created by rf9 on 16/06/2016.
 */
class LabwareTypesTest extends Specification {

    def "can list the material types"() {
        given:
        def restService = new RestService(RestServiceConfig.containerServiceUrl)
        def converter = new ResourceConverter(LabwareType.class, Layout.class, Location.class)
        List<LabwareType> labwareTypes = converter.readObjectCollection(restService.get('api/v1/labware_types/', [:]).getBytes(), LabwareType.class)

        def labwareType
        when:
        labwareType = labwareTypes.find { it.name == labwareTypeName }

        then:
        labwareType != null
        labwareType.layout.locations.size() == locationsSize

        where:
        labwareTypeName          | locationsSize
        'generic tube'           | 1
        'generic 96 well plate'  | 96
        'generic 384 well plate' | 384
    }
}
