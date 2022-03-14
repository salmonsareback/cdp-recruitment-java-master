package adeo.leroymerlin.cdp.cucumber.steps;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class CucumberBootStrap {

  private static final Logger LOG = LoggerFactory.getLogger(CucumberBootStrap.class);

  // Get package of models from application.properties file
//  @Value("${cucumber.utils.models.package}")
//  String packageModelsPath;


//
//    /**
//     * Need this method so the cucumber will recognize this class as glue and load spring context configuration
//     */
//    @Before
//    public void setUp() {
//        LOG.info("-------------- Spring Context Initialized For Executing Cucumber Tests --------------");
//    }
//    @Autowired
//    private CustomerRepository customerRepository;
//    @Autowired
//    private LedgerRepository ledgerRepository;
//    @Autowired
//    private LedgerBalanceRepository ledgerBalanceRepository;

  //Method annotated with @After executes after every scenario
//    @After
//    public void cleanUp() {
//        LOG.info(">>> cleaning up after scenario!");
//        customerRepository.deleteAll();
//    }

  //Method annotated with @AfterStep executes after every step
//    @AfterStep
//    public void afterStep() {
//        LOG.info(">>> AfterStep!");
//        //placeholder for after step logic
//    }

//    //Method annotated with @Before executes before every scenario
//    @Before
//    public void before() {
//        LOG.info(">>> Before scenario!");
//        ledgerBalanceRepository.deleteAllInBatch();
//        ledgerRepository.deleteAllInBatch();
//        //placeholder for before scenario logic
//    }
//
//    @Before("@skip_scenario")
//    public void skip_scenario(Scenario scenario){
//        LOG.info("SKIP SCENARIO: " + scenario.getName());
//        Assume.assumeTrue(false);
//    }
//
//    //Method annotated with @BeforeStep executes before every step
//    @BeforeStep
//    public void beforeStep() {
//        LOG.info(">>> BeforeStep!");
//        //placeholder for beforestep scenario logic
//
//    }
}
