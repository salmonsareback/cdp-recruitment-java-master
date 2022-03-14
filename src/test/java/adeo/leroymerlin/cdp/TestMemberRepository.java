package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Member;
import adeo.leroymerlin.cdp.repositories.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class TestMemberRepository {
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void membersNamedClWithTheirBand_doNotFails(){
        List<Member> members = memberRepository.memberHavingPatternedNameAndItsBands("%Ge%");
        assertThat(members).isNotNull();
    }
}
