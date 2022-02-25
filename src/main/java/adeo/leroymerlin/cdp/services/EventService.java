package adeo.leroymerlin.cdp.services;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.DTO.*;
import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.BandRepository;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    private BandRepository bandRepository;

    @Autowired
    BandService bandService;

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

    public List<EventWithBandOfIdolDto> getFilteredEvents(String idolFewLetters) {
        // Get bands including members with patterned name
        List<BandWithFilteredMembersDto> bands = bandService.bandsWithNamedMembers(idolFewLetters);

        // Preformat the bands list with Members
        List<BandWithIdolAndIdDto> bandsWithIdols = bands.stream().map(band -> formatBandWithMember(band)).collect(Collectors.toList());


        // Get events of those bands
        List<Long> bandsIdentifier = bands.stream().map(band -> band.getId()).collect(Collectors.toList());
        List<Band> someBandsShowsEvents = listOfBandIdentifiersWithEvents(bandsIdentifier);

        // Transform list respecting Event structure with relations
        List<Event> eventsForIdols = new ArrayList<>();

        someBandsShowsEvents.stream().forEach(band1 -> {
            band1.getEvents().forEach(event2 -> {
                // Check if event already listed
                if (eventsForIdols.stream().filter(event3 -> event3.getId() == event2.getId()).count() == 0) {
                    Event oneEventForIdols = new Event(event2.getId(), event2.getTitle(), event2.getImgUrl());
                    eventsForIdols.add(oneEventForIdols);
                }
            });
        });

        // Combine eventsForIdols and band respecting output format
        // Output formats are designed in Dto
        List<EventWithBandOfIdolDto> eventWithBandOfIdol = new ArrayList<>();

        eventsForIdols.stream()
                .forEach(event4 -> {
                    // Initiate the formated event
                    EventWithBandOfIdolDto oneEvent = new EventWithBandOfIdolDto(event4.getTitle(), event4.getImgUrl());
                    // Build list of band for oneEvent
                    List<BandWithIdolDto> bandsOfTheEvent = someBandsShowsEvents.stream()
                            // gives a list of bands showing the event4 :
                            .filter(band5 -> bandContainsEventId(band5, event4.getId()))
                            // gives the list of pre-formated bands :
                            .map(band9 -> {
                                return selectBandWithIdolAndId(band9.getId(), bandsWithIdols);
                            })
                            // gives the list of bands without id to respect output format
                            .map(band11 -> new BandWithIdolDto(band11.getName() + " [" + band11.getMembers().size() + "]", band11.getMembers()))
                            .collect(Collectors.toList());
                    // Set formated band to event
                    oneEvent.setBands(bandsOfTheEvent);
                    // Count bands in event
                    oneEvent.setTitle(oneEvent.getTitle() + " [" + oneEvent.getBands().size() + "]");
                    // Add formated event to final
                    eventWithBandOfIdol.add(oneEvent);
                });
        return eventWithBandOfIdol;

    }

    public List<Event> getFilteredEventsGD(String query) {
        List<Event> events = eventRepository.findAllBy();
        if (query == null) { // no search query, we return all events
            return events;
        }
        return events.stream()
                .filter(
                        event ->
                                event.getBands().stream()
                                        .anyMatch(
                                                band ->
                                                        band.getMembers().stream()
                                                                .anyMatch(member ->
                                                                                member.getName().toUpperCase(Locale.ROOT).contains(query.toUpperCase(Locale.ROOT))
                                                                )
                                        )
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public Event create(Event event) {
        // Let's be sure that the id is null
        event.setId(null);
        // We should not create an event and link bands at same time
        event.setBands(null);
        return eventRepository.save(event);
    }

    @Transactional
    public Event update(Event event) {
        Optional<Event> previous = eventRepository.findById(event.getId());
        if (previous.isPresent()) {
            // Preserve url image if not a new url
            if ((event.getImgUrl() == null || event.getImgUrl().isBlank()) && (previous.get().getImgUrl() != null && !previous.get().getImgUrl().isBlank())) {
                event.setImgUrl(previous.get().getImgUrl());
            }
            return eventRepository.save(event);
        } else {
            return null;
        }
    }

    @Transactional
    public Event removeBandFromEvent(Long event_id, Long band_id) {
        return eventRepository.removeBandByIds(event_id, band_id);
    }

    @Transactional
    public Event addBandToEvent(Long event_id, Long band_id) {
        Optional<Band> bandToAdd = bandRepository.findById(band_id);
        if (eventRepository.findById(event_id) != null && bandToAdd.isPresent()) {
            return eventRepository.addBandToEventId(event_id, bandToAdd.get());
        } else {
            return null;
        }
    }

    public List<Band> listOfBandIdentifiersWithEvents(List<Long> identifiers) {
        List<Band> eventsWithselectionOfBandsWithEvents = bandRepository.bandsWithEventsFromAListOfBandIdentifiers(identifiers);
        return eventsWithselectionOfBandsWithEvents;
    }

    private BandWithIdolAndIdDto formatBandWithMember(Band band) {
        BandWithIdolAndIdDto bandWithIdolAndId = new BandWithIdolAndIdDto(band.getId(), band.getName());
        bandWithIdolAndId.setMembers(
                band.getMembers().stream().map(member -> new IdolDto(member.getName())).collect(Collectors.toList())
        );
        return bandWithIdolAndId;
    }

    private Boolean bandContainsEventId(Band band, Long eventId) {
        // Band with event contain eventId
        List<Long> eventIdsOfBand = band.getEvents().stream().map(event -> event.getId()).collect(Collectors.toList());
        return eventIdsOfBand.contains(eventId);
    }

    private BandWithIdolAndIdDto selectBandWithIdolAndId(Long bandId, List<BandWithIdolAndIdDto> bandsWithIdols) {
        return bandsWithIdols.stream().filter(band -> band.getId().equals(bandId)).findFirst().orElse(new BandWithIdolAndIdDto(0L, "Unjoigned"));
    }

    @Transactional
    public Event createEventWithBandIds(String title, Integer nbStars, Set<Long> bandsIds) {
        var event = new Event(title, nbStars, "");
        Set<Band> bands = bandsIds.stream().map(bandId -> bandRepository.getById(bandId)).collect(Collectors.toSet());
        event.setBands(bands);
        return eventRepository.save(event);
    }
}
