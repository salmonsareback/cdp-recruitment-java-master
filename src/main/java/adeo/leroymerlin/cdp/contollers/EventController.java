package adeo.leroymerlin.cdp.contollers;

import adeo.leroymerlin.cdp.DTO.EventWithBandOfIdolDto;
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
    public List<EventWithBandOfIdolDto> findEvents(@PathVariable String query) {
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

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public Event createEvent( @RequestBody Event event) {
        return eventService.create(event);
    }


    @PutMapping(value="/{event_id}/showsband/{band_id}")
    public Event showsBand(@PathVariable Long event_id,@PathVariable Long band_id){
        Event updatedEvent = eventService.addBandToEvent(event_id, band_id);
        return updatedEvent;
    }

    @DeleteMapping(value="/{event_id}/doesnotshowband/{band_id}")
    public Event notShowsBand(@PathVariable Long event_id,@PathVariable Long band_id){
        Event updatedEvent = eventService.removeBandFromEvent(event_id,band_id);
        if(updatedEvent == null) throw new HttpClientErrorException(HttpStatus.NOT_FOUND,"Event id "+event_id+" not found or not including band "+band_id);
        return updatedEvent;
    }}
