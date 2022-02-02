package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SetOfBandSerializer  extends StdSerializer<Set<Band>> {

        public SetOfBandSerializer() {
            this(null);
        }

        public SetOfBandSerializer(Class<Set<Band>> t) {
            super(t);
        }

        @Override
        public void serialize(
                Set<Band> bands,
                JsonGenerator generator,
                SerializerProvider provider)
                throws IOException, JsonProcessingException {

            List<String> names = new ArrayList<>();
            for (Band band : bands) {
                names.add(band.getName());
            }
            generator.writeObject(names);
        }

}
