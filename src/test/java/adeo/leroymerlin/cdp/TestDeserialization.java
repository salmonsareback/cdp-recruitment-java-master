package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDeserialization {

    @Test
    public void givenBidirectionRelation_whenSerializing_thenException()
            throws JsonProcessingException {

        Event event = new Event(1L,"Jazz in Lille", 2,"No comment");
        Band band = new Band( 2L, "book", event);
        event.addBand(band);

        new ObjectMapper().writeValueAsString(event);
    }
}
