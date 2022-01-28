package adeo.leroymerlin.cdp.contollers;

import adeo.leroymerlin.cdp.services.EventService;
import adeo.leroymerlin.cdp.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping(value="/test", method=RequestMethod.GET)
    public String test(){
        return "Hello, this is a test";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Event> findEvents() {
        return eventService.getEvents();
    }

    @RequestMapping(value = "/search/{query}", method = RequestMethod.GET)
    public List<Event> findEvents(@PathVariable String query) {
        return eventService.getFilteredEvents(query);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteEvent(@PathVariable Long id) {
        eventService.delete(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Event updateEvent(@PathVariable Long id, @RequestBody Event event) {
        if(!event.getId().equals(id)) throw new HttpClientErrorException(HttpStatus.CONFLICT, "Inconsistant identifier as parameter and in body");
        Event updatedEvent = eventService.update(event);
        if(updatedEvent == null) throw new HttpClientErrorException(HttpStatus.NOT_MODIFIED,"Unable to update event");
        return event;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Event updateEvent( @RequestBody Event event) {
        return eventService.create(event);
    }
}
