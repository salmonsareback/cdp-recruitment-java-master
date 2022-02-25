package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.DTO.EventWithBandOfIdolDto;
import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.services.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
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

    @Test
    public void bands1000And1002ShowEvents(){
        List<Long> identifiers =  Arrays.asList(1000L,1002L);
        List<Band> bands = eventService.listOfBandIdentifiersWithEvents(identifiers);
        assertThat(bands.size()).isGreaterThan(0);

    }

    @Test
    public void eventsShowingIdolLikeCo_returnMemberCo(){
        List<EventWithBandOfIdolDto> eventsForCo = eventService.getFilteredEvents("Co");
        assertThat(eventsForCo.get(0).getBands().get(0).getMembers().get(0).getName()).contains("Co");
    }

    @Test
    public void eventsShowingIdolLikeGe_is2(){
        List<EventWithBandOfIdolDto> eventsForGe = eventService.getFilteredEvents("Ge");
        assertThat(eventsForGe.size()).isEqualTo(2);
    }
}
