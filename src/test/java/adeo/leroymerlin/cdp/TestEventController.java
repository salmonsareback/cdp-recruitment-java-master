package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.contollers.EventController;
import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.BandRepository;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.test.annotation.TestAnnotationUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc
public class TestEventController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    BandRepository bandRepository;

    @Test
    public void createNewEvent_Returns200(
    ) throws Exception {
        Event event = new Event("This is a new event",3,"This is the comment of new event");
            this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event))
            ).andExpect(status().isOk());
    }

    @Test
    public void addBand1000ToEvent1001() throws Exception {
        this.mockMvc.perform(put("/api/events/1001/showsband/1000"));
        Optional<Event> event = this.eventRepository.findById(1001L);
        assertThat(event).isPresent();
        Band band1000 =bandRepository.findById(1000L).get();
        assertThat(event.get().getBands().contains(band1000));
    }
}
