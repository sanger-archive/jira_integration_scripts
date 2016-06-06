package converters
/**
 * See README.md for copyright details
 */

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.jasminb.jsonapi.ResourceConverter
import models.*

/**
 * The {@code MaterialBatchConverter} class represents a converter that converts a
 * JSON-API based json document to the appropriate object(s).
 * This converter created a {@code MaterialBatch} object and its relations, what was
 * represented in the json document.
 * 
 * @author ke4
 *
 */
class MaterialBatchConverter {

    static ResourceConverter materialBatchConverter;
    static {
        ObjectMapper materialMapper = new ObjectMapper();
        // TODO register only JavaTimeModule
//        materialMapper.registerModule(new JavaTimeModule());
        materialMapper.findAndRegisterModules();
        materialMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        materialMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        materialBatchConverter =
            new ResourceConverter(materialMapper, MaterialBatch.class, 
                Material.class, Metadatum.class, MaterialType.class);
    }

    static MaterialBatch convertJsonToObject(String materialBatchJson) {
        return materialBatchConverter.readObject(materialBatchJson.getBytes(), MaterialBatch.class);
    }
}
