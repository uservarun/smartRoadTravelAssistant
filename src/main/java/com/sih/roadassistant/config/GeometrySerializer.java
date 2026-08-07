package com.sih.roadassistant.config;

import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import org.springframework.boot.jackson.JacksonComponent;
@JacksonComponent
public class GeometrySerializer extends ValueSerializer<Geometry> {
    
    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializationContext serializers) {
        try {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toText());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JTS geometry to WKT", e);
        }
    }
}