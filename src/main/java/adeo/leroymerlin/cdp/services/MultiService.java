package adeo.leroymerlin.cdp.services;

import adeo.leroymerlin.cdp.DTO.BandWithFilteredMembersDto;
import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MultiService {
    @Autowired
    EventService eventService;

    @Autowired
    BandService bandService;

    public void eventsShowingArtistsLike(String artistFewLetters){
        // Get bands including members with patterned name
        List<BandWithFilteredMembersDto> bands = bandService.bandsWithNamedMembers(artistFewLetters);
        // Get events of those bands
        List<Long> bandsIdentifier = bands.stream().map(band -> band.getId()).collect(Collectors.toList());
        List<Band> someBandsShowsEvents = eventService.listOfBandIdentifiersWithEvents (bandsIdentifier);
        // Transform
        // TODO should use a Dto not to show identifiers neither number of stars
        List<Event> eventsForIdols = new ArrayList<Event>();

        someBandsShowsEvents.stream().forEach(band1 ->{
            // Clone

            band1.getEvents().forEach(event2 -> {
            // Check if event already listed
                if(eventsForIdols.stream().filter(event3 -> event3.getId() == event2.getId()).count() == 0 ){
                    Event oneEventForIdols = new Event(event2.getTitle(), event2.getImgUrl());
                    eventsForIdols.add(oneEventForIdols);
                }
                // Add band and idol

            });
        });
    }
}
