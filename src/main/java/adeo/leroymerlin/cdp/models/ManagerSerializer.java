package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ManagerSerializer extends StdSerializer<Manager> {

        public ManagerSerializer() {
            this(null);
        }

        public ManagerSerializer(Class<Manager> t) {
            super(t);
        }

        @Override
        public void serialize(
                Manager manager,
                JsonGenerator generator,
                SerializerProvider provider)
                throws IOException, JsonProcessingException {

                    generator.writeObject("Managed by "+manager.getFirstName());
        }

}
