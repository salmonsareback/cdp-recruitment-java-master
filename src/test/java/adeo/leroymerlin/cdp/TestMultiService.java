package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.DTO.EventWithBandOfIdolDto;
import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.services.EventService;
import adeo.leroymerlin.cdp.services.MultiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
public class TestMultiService {

    @Autowired
    MultiService multiService;

    @Test
    public void eventsShowingIdolLikeGE_is2(){
        List<EventWithBandOfIdolDto> eventsForGe = multiService.eventsShowingArtistsLike("Ge");
        assertThat(eventsForGe.size()).isEqualTo(2);
    }
}
