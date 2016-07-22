package uk.ac.sanger.scgcf.jira.services.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Created by ke4 on 19/07/2016.
 */
public class JsonObjectMapper extends ObjectMapper {

    public JsonObjectMapper() {
        SimpleModule sm = new SimpleModule("LocalDateModule", new Version(1,1,1,""));
        sm.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                ToStringSerializer.instance.serialize(value, jgen, provider);
            }
        });
        sm.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                ToStringSerializer.instance.serialize(value, jgen, provider);
            }
        });
        sm.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return LocalDate.parse(jp.getText());
            }
        });
        sm.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return LocalDateTime.parse(jp.getText());
            }
        });
        registerModule(sm);
    }
}