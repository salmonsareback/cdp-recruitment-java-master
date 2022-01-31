package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.services.EventService;
import org.hsqldb.util.DatabaseManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
public class TestEventService {

    @Autowired
    EventService eventService;

    @Autowired
    EventRepository eventRepository;


    @Test
    public void createEventWithService(){
        Event event = new Event("Jazz Lille",3, "No comments");
        Event newEvent = eventService.create(event);
        assertThat(newEvent.getId()).isNotNull();
        assertThat(newEvent.getTitle()).isEqualTo("Jazz Lille");
    }

    @Test
    public void updateEventWithService(){
        Event event = new Event(1000L,"GrasRock Metal Meeting",2, "Not Pop, it is rock & roll !");
        Event newEvent = eventService.update(event);
        assertThat(newEvent.getId()).isEqualTo(1000L);
        assertThat(newEvent.getTitle()).isEqualTo("GrasRock Metal Meeting");
    }

    @Test
    public void deletingEventByIdWithServiceIsFlushed(){
        eventService.delete(1004L);
        Optional<Event> event=eventRepository.findById(1004L);
        assertThat(event.isPresent()).isFalse();

    }
}
