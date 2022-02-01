package adeo.leroymerlin.cdp.repositories;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.models.Member;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface MemberRepository extends Repository<Member, Long> {

    // Jockers could be %, i.e %Hel% search for name including (start, include, end by) 'Hel')
    List<Member> memberHavingPatternedNameAndItsBands(
            @Param("fewLetters") String fewLettersOfMemberNameIncludingJockers
    );
}
