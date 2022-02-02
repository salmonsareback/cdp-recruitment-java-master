package adeo.leroymerlin.cdp.services;

import adeo.leroymerlin.cdp.DTO.*;
import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MultiService {
    @Autowired
    EventService eventService;

    @Autowired
    BandService bandService;

    private BandWithIdolAndIdDto formatBandWithMember(Band band){
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

    private BandWithIdolAndIdDto selectBandWithIdolAndId(Long bandId,List<BandWithIdolAndIdDto> bandsWithIdols){
        return bandsWithIdols.stream().filter(band->band.getId().equals(bandId)).findFirst().orElse(new BandWithIdolAndIdDto(0L,"Unjoigned"));
    }

    public List<EventWithBandOfIdolDto> eventsShowingArtistsLike(String artistFewLetters) {
        // Get bands including members with patterned name
        List<BandWithFilteredMembersDto> bands = bandService.bandsWithNamedMembers(artistFewLetters);

        // Preformat the bands list with Members
        List<BandWithIdolAndIdDto> bandsWithIdols = bands.stream().map(band -> formatBandWithMember(band)).collect(Collectors.toList());


        // Get events of those bands
        List<Long> bandsIdentifier = bands.stream().map(band -> band.getId()).collect(Collectors.toList());
        List<Band> someBandsShowsEvents = eventService.listOfBandIdentifiersWithEvents(bandsIdentifier);

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
                    .filter(band5 -> bandContainsEventId(band5,event4.getId()))
                    // gives the list of pre-formated bands :
                    .map(band9 -> {
                        return selectBandWithIdolAndId(band9.getId(), bandsWithIdols);
                    })
                    // gives the list of bands without id to respect output format
                    .map(band11 -> new BandWithIdolDto(band11.getName(),band11.getMembers()))
                    .collect(Collectors.toList());
                    // Set formated band to event
                    oneEvent.setBands(bandsOfTheEvent);
                    // Add formated event to final
                    eventWithBandOfIdol.add(oneEvent);
                });
        return eventWithBandOfIdol;
    }
}
