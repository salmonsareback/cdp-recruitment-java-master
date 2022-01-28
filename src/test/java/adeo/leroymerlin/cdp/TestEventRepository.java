package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.services.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
public class TestEventRepository {

    @Autowired
    EventRepository eventRepository;

    @Test
//     Database is refreshed and populated at each run
//     Event 10001 is populated
    public void eventHaveBeenPopulatedTestedByRepository(){
        Optional<Event> event=eventRepository.findById(1001L);
        assertThat(event.isPresent()).isTrue();
    }
    @Test
    public void deletingEventByIdWithRepository(){
        eventRepository.deleteById(1004L);
        Optional<Event> event=eventRepository.findById(1004L);
        assertThat(event.isPresent()).isFalse();

    }

//    @Test
//    public void removeEventByIdWithRepository(){
////        org.hsqldb.util.DatabaseManagerSwing.main(new String[] {
////                "--url",  "jdbc:hsqldb:mem:testdb", "--noexit"
////        });
//        eventRepository.removeById(1001L);
//        Optional<Event> event=eventRepository.findById(1001L);
//        assertThat(event.isPresent()).isFalse();
//
//    }

}
