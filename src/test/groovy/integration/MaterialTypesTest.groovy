package integration

import com.github.jasminb.jsonapi.ResourceConverter
import spock.lang.Specification
import uk.ac.sanger.scgcf.jira.services.models.MaterialType
import uk.ac.sanger.scgcf.jira.services.utils.RestService
import uk.ac.sanger.scgcf.jira.services.utils.RestServiceConfig

/**
 * Created by rf9 on 16/06/2016.
 */
class MaterialTypesTest extends Specification {

    def "can list the material types"() {
        setup:
        def restService = new RestService(RestServiceConfig.materialServiceUrl)
        def converter = new ResourceConverter(MaterialType.class)

        when:
        List<MaterialType> materialTypes = converter.readObjectCollection(restService.get('api/v1/material_types/', [:]).getBytes(), MaterialType.class)

        then:
        'sample' in materialTypes*.name
        'library' in materialTypes*.name
    }
}
