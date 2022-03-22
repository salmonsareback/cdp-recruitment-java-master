package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface MemberRepository extends JpaRepository<Member, Long> {
    // JpaRepository offers much more : paging, getReference, ...

    // Jockers could be %, i.e %Hel% search for name including (start, include, end by) 'Hel')
    List<Member> memberHavingPatternedNameAndItsBands(
            @Param("fewLetters") String fewLettersOfMemberNameIncludingJockers
    );


    Optional<Member> findById(Long id);



}
