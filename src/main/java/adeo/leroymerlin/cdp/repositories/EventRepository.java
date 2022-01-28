package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends Repository<Event, Long> {

    // Implement findById
    Event findById(Long eventId);

    void deleteById(Long eventId);


    default void removeById(Long eventId){
        Event event = this.findById(eventId);
        if(event!=null){
            for(Band band : event.getBands()){
                event.removeBand(band);
            }
            this.deleteById(eventId);
        }
    }

    List<Event> findAllBy();
}
