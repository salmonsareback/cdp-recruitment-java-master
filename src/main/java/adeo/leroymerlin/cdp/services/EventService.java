package adeo.leroymerlin.cdp.services;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents() {
        return eventRepository.findAllBy();
    }

    @Transactional
    public void delete(Long id) {

        eventRepository.deleteById(id);
//        eventRepository.removeById(id);
    }

    public List<Event> getFilteredEvents(String query) {
        List<Event> events = eventRepository.findAllBy();
        // Filter the events list in pure JAVA here

        return events;
    }

    @Transactional
    public Event create(Event event){
        // Let's be sure that the id is null
        event.setId(null);
        // We should not create an event and link bands at same time
        event.setBands(null);
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(Event event){
        Optional<Event> previous = eventRepository.findById(event.getId());
        if(previous.isPresent()) {
            // Preserve url image
            if((event.getImgUrl()==null || event.getImgUrl().isBlank()) && (previous.get().getImgUrl() != null && !previous.get().getImgUrl().isBlank())){
                event.setImgUrl(previous.get().getImgUrl());
            }
            return eventRepository.save(event);
        }else{
            return null;
        }
    }
}
