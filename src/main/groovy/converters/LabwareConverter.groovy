package converters
/**
 * See README.md for copyright details
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jasminb.jsonapi.ResourceConverter;

import models.Labware;
import models.LabwareType;
import models.Layout;
import models.Location;
import models.Metadatum;
import models.Receptacle;

/**
 * The {@code LabwareConverter} class represents a converter that converts a
 * JSON-API based json document to the appropriate object(s).
 * This converter created a {@code Labware} object and its relations, what was
 * represented in the json document.
 * 
 * @author ke4
 *
 */
class LabwareConverter {

    static ResourceConverter labwareConverter;
    static {
        ObjectMapper labwareMapper = new ObjectMapper();
        // TODO register only JavaTimeModule
//        materialMapper.registerModule(new JavaTimeModule());
        labwareMapper.findAndRegisterModules();
        labwareMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        labwareMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        labwareConverter =
            new ResourceConverter(labwareMapper, Labware.class, LabwareType.class, 
                Layout.class, Location.class, Metadatum.class, Receptacle.class);
    }

    static Labware convertJson(String plateJson) {
        return labwareConverter.readObject(plateJson.getBytes(), Labware.class);
    }
}
