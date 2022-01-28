package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Event;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface EventRepository extends Repository<Event, Long> {


    @Transactional
    void deleteById(Long eventId);

    Optional<Event> findById(Long eventId);

//     @Transactional
//    default void removeById(Long eventId){
//        Optional<Event> event = this.findById(eventId);
//        if(event.isPresent()){
//            for(Band band : event.get().getBands()){
//                event.get().removeBand(band);
//            }
//            this.delete(event.get());
//        }
//    }

    List<Event> findAllBy();

}
