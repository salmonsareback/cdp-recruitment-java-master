package adeo.leroymerlin.cdp.cucumber.steps;

import adeo.leroymerlin.cdp.models.Band;
import adeo.leroymerlin.cdp.models.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.getRecursivelySuperClassDeclaredField;

public class TestRecursiveFieldSearch {

    @Test
    public void testRecursive() throws NoSuchFieldException {


        var test = getRecursivelySuperClassDeclaredField(Event.class, "title");

    }
}
