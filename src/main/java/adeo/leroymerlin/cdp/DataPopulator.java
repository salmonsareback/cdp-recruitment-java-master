package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.models.Member;
import adeo.leroymerlin.cdp.repositories.BandRepository;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import adeo.leroymerlin.cdp.repositories.MemberRepository;
import adeo.leroymerlin.cdp.services.BandService;
import adeo.leroymerlin.cdp.services.EventService;
import com.github.javafaker.Faker;
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
    private EventService eventService;
    @Autowired
    EntityManager entityManager;

    @Override
    public void run(String... args) throws Exception {
        if (Arrays.stream(args).filter(arg -> arg.equals("--populate")).count() > 0) {
            final int NB_MEMBER = 10000;
            final int NB_BAND = 10000;
            final int NB_EVENT = 20000;
            Random random = new Random();
            System.out.println("Generating test data...");
            Faker faker = new Faker();

            // Populate members
            for (Integer i = 0; i < NB_MEMBER; i++) {
                memberRepo.save(new Member(faker.name().fullName()));
            }
            // Populate bands
            for (Integer i = 0; i < NB_BAND; i++) {
                // A band could content 1 to 6 members
                Integer nbMembers = (int) (random.nextInt(5) + 1);
                Set<Long> membersIds = new HashSet<>();
                for (Integer j = 0; j < nbMembers; j++) {
                    membersIds.add(random.nextLong(1001L, 1001L + NB_MEMBER));
                }
                bandService.createBandWithMemberIds(faker.dog().name() + " band", membersIds);
            }

            for (Integer i = 0; i < NB_EVENT; i++) {
                // An event could stage 2 to 8 bands
                Integer nbBands = (int) (random.nextInt(2, 8));
                Set<Long> bandsIds = new HashSet<>();
                for (Integer j = 0; j < nbBands; j++) {
                    bandsIds.add(random.nextLong(1001L, 1001L + NB_BAND));
                }

                String eventName = faker.music().genre() + " in " + faker.address().cityName();
                eventService.createEventWithBandIds(eventName, random.nextInt(1, 5), bandsIds);
            }
            System.out.println("Demo data generated");
        }
    }
}

