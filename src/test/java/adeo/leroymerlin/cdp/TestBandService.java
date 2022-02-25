package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.DTO.BandWithFilteredMembersDto;
import adeo.leroymerlin.cdp.services.BandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class TestBandService {

    @Autowired
    BandService bandService;

    @Test
    public void bandsWithNamedMembersLikeGe_ShouldReturn2Bands(){
        List<BandWithFilteredMembersDto> bands = bandService.bandsWithNamedMembers("Ge");
        assertThat(bands.size()).isEqualTo(2);
    }

    @Test
    public void bandsWithNamedMembersLikeGE_isCaseSensitive(){
        List<BandWithFilteredMembersDto> bands = bandService.bandsWithNamedMembers("GE");
        assertThat(bands.size()).isEqualTo(0);
    }
}
