package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Band;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

//@Transactional(readOnly = true)
public interface BandRepository extends JpaRepository<Band, Long> {
    // JpaRepository offers much more : paging, getReference, ...


    Optional<Band> findById(Long id);

    // Jockers could be %, i.e %Hel% search for name including (start, include, end by) 'Hel')
    List<Band> bandAndAllMembersWithAtLeastOneMemberHasNameLike(
            @Param("fewLetters") String fewLettersOfMemberNameIncludingJockers
    );

    List<Band> bandsWithEventsFromAListOfBandIdentifiers(
            @Param("listOfBandsIdentifiers") List<Long> listOfBandsIdentifiers
    );

    Band save(Band band);

}
