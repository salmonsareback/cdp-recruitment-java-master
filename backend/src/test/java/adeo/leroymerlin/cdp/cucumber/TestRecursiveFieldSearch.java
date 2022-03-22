package adeo.leroymerlin.cdp.cucumber;

import adeo.leroymerlin.cdp.models.Event;
import org.junit.jupiter.api.Test;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.getRecursivelySuperClassDeclaredField;

public class TestRecursiveFieldSearch {

    @Test
    public void testRecursive() throws NoSuchFieldException {


        var test = getRecursivelySuperClassDeclaredField(Event.class, "title");

    }
}
