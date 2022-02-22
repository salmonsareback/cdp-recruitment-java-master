package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.models.Member;
import adeo.leroymerlin.cdp.repositories.BandRepository;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.repositories.MemberRepository;
import adeo.leroymerlin.cdp.services.BandService;
import com.github.javafaker.Faker;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.*;

@Component
public class DataPopulator implements CommandLineRunner {

//	private static final Logger LOG = (Logger) LoggerFactory.getLogger(DataPopulator.class);

	@Autowired
	private MemberRepository memberRepo;
	@Autowired
	private BandRepository bandRepo;
	@Autowired
	private EventRepository eventRepo;
	@Autowired
	private BandService bandService;
	@Autowired
	EntityManager entityManager;

	@Override
	public void run(String... args) throws Exception {
		if(Arrays.stream(args).filter(arg-> arg.equals("--populate")).count()>0) {
			final int NB_MEMBER = 30;
			final int NB_BAND = 10;
			final int NB_EVENT = 20;
			System.out.println("Generating test data...");
			Faker faker = new Faker();
			// Populate members
			for (Integer i = 0; i < NB_MEMBER; i++) {
				memberRepo.save(new Member(faker.name().fullName()));
			}
			// Populate bands
			Random bandR = new Random();
			for(Integer i=0; i<NB_BAND;i++){
				Integer nbMembers =(int) (bandR.nextInt(5)+1);
				Set<Long> membersIds = new HashSet<>();
				for(Integer j = 0; j<nbMembers; j++){
					membersIds.add(bandR.nextLong(1001L,1001L+NB_MEMBER));
				}
				bandService.createBandWithMemberIds(faker.dog().name()+" band", membersIds);
			}

/*
			Random nbStar = new Random();
			for (Integer i = 0; i < 10000; i++) {
				String eventName = faker.music().genre() + " in " + faker.address().cityName();
				eventRepo.save(new Event(eventName, (int) (nbStar.nextInt() * 5), ""));
			}
*/
			System.out.println("Demo data generated");
			//		}
		}
	}
}

