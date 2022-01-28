package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.services.EventService;
import org.hsqldb.util.DatabaseManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
public class TestEventService {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventService eventService;

    @Test
    // Database is refreshed and populated at each run
    // Event 10001 is populated
    public void eventHaveBeenPopulatedTestedByRepository(){
        Event event=eventRepository.findById(1001L);
        assertThat(event.getId()).isEqualTo(1001L);
    }
    @Test
    public void deletingEventByIdWitRepository(){
        eventRepository.deleteById(1005L);
        Event event=eventRepository.findById(1005L);
        assertThat(event).isNull();

    }

    @Test
    public void removeEventByIdWitRepository(){
//        org.hsqldb.util.DatabaseManagerSwing.main(new String[] {
//                "--url",  "jdbc:hsqldb:mem:testdb", "--noexit"
//        });
        eventRepository.removeById(1001L);
        Event event=eventRepository.findById(1001L);
        assertThat(event).isNull();

    }
}
