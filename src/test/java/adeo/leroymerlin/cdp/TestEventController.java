package adeo.leroymerlin.cdp;

import adeo.leroymerlin.cdp.contollers.EventController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestEventController {

    //  https://spring.io/guides/gs/testing-web/
    @Autowired
    private EventController eventController;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void sanityCheck(){
        assertThat(eventController).isNotNull();
    }

    public void createNewEvent(){

    }
}
