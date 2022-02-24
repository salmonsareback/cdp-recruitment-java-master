package adeo.leroymerlin.cdp.cucumber.steps;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.assertMappedCucumberTableEqualEntityInstances;
import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.newEntityInstanceFromMap;

public class EventSteps {
  // TODO fetch models package
  String packageModelsPath ="adeo.leroymerlin.cdp.models";

  private List<Event> foundEvents;

  //    @Autowired
//    private HttpClient httpClient;
//
  @Autowired
  EventRepository eventRepo;

  @DataTableType(replaceWithEmptyString = "[blank]")
  public Event eventEntry(Map<String, String> entry) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    return newEntityInstanceFromMap(entry, Event.class);
  }

  @Given("^the following list of events:$")
  public void instance_events(List<Event> events) throws Exception {
    eventRepo.saveAll(events);
  }

  @Then("the events count is {int}")
  public void theLedgersCountIs(int countEvents) {
    int i = (int) eventRepo.count();
    Assertions.assertThat(i).isEqualTo(countEvents);
  }

  @Then("^the last events can be found :$")
  public void last_events_in_database(List<Map<String, String>> rows) throws Throwable {
    // TODO generalize assertClassEntityPersistedDataLikeCucumberTable(Event.class,EventRepository.class,"findByOrderByIdDesc",true,rows);
    // Fetch ledger in database
    Page<Event> eventsPersistedPage = eventRepo.findByOrderByIdDesc(PageRequest.of(0, rows.size()));
    // Reverse content of returned page
    List<Event> eventsPersisted = eventsPersistedPage.getContent().stream().collect(Collectors.toList());
    Collections.reverse(eventsPersisted);
    assertMappedCucumberTableEqualEntityInstances(eventsPersisted, rows);

  }
}
