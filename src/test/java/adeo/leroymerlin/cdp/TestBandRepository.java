package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.repositories.BandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestBandRepository {
    @Autowired
    BandRepository bandRepository;

    @Test
    public void bandsWithMemberLikeQu(){
        List<Band> bands=bandRepository.bandAndAllMembersWithAtLeastOneMemberHasNameLike("%Cl%");
    }
}
