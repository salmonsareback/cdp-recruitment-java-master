package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface EventRepository extends JpaRepository<Event, Long> {
    // Jpa repository brings much more than Repository

    List<Event> findAllBy();

    Optional<Event> findById(Long eventId);

    void deleteById(Long eventId);


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

    public default Event removeBandByIds(Long event_id, Long band_id){
        Optional<Event> event = this.findById(event_id);
        if(event.isPresent()) {
            Band bandToRemove = event.get().getBands().stream().filter(band -> band.getId().equals(band_id)).findFirst().orElse(null);
            if (bandToRemove != null) {
                event.get().removeBand(bandToRemove);
            }
            return event.get();
        }else{
            return null;
        }
    }

    default Event addBandToEventId(Long event_id, Band band){
        // Band is passed so as not to use band repository in event repository
        Optional<Event> event = this.findById(event_id);
        try {
            event.get().addBand(band);
            return this.findById(event_id).get();
        }catch (Exception e){
            return null;
        }
    }

    Event save(Event event);

    Page<Event> findByOrderByIdDesc(Pageable pageable);


}
