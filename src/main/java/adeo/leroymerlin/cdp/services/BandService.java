package adeo.leroymerlin.cdp.services;

import adeo.leroymerlin.cdp.DTO.BandWithFilteredMembersDto;
import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Member;
import adeo.leroymerlin.cdp.repositories.BandRepository;
import adeo.leroymerlin.cdp.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class BandService {

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	BandRepository bandRepository;

	public List<BandWithFilteredMembersDto> bandsWithNamedMembers(String includedTerm) {
		// Output a list of band and filtered counted members
		List<BandWithFilteredMembersDto> bands = new ArrayList<>();

		// Fetch members respecting a patterned name and their bands
		List<Member> members = memberRepository.memberHavingPatternedNameAndItsBands("%" + includedTerm + "%");

		// Inverse list
		// From members including bands to bands including members
		members.stream().forEach(member -> {
			// Clone member to first level
			Member memberToAdd = new Member(member.getId(), member.getName());
			member.getBands().forEach(band -> {
				// Check if Band already listed
				if (bands.stream().filter(band3 -> band3.getId() == band.getId()).count() == 0) {
					BandWithFilteredMembersDto newBand = new BandWithFilteredMembersDto(band.getId(), band.getName(), 0);
					bands.add(newBand);
				}
				// Add member to band
				BandWithFilteredMembersDto bandToAddMember = bands.stream().filter(band2 -> band2.getId() == band.getId()).findFirst().get();
				bandToAddMember.getMembers().add(memberToAdd);
				// TODO useless counting, size() does the same !
				bandToAddMember.setNbOfMembers(bandToAddMember.getNbOfMembers() + 1);
			});
		});

		return bands;
	}


	public Band createBandWithMemberIds(String name, Set<Long> membersIds) {
		var band = new Band(name);
		// getById works by reference and will avoid a lot of queries to select data
		Set<Member> members =
		  membersIds.stream().map(memberId ->  memberRepository.getById(memberId)).collect(Collectors.toSet());
		band.setMembers(members);
		return bandRepository.save(band);
	}

}
