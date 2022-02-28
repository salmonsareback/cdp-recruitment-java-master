package adeo.leroymerlin.cdp.cucumber.steps;

import adeo.leroymerlin.cdp.models.Event;
import adeo.leroymerlin.cdp.repositories.EventRepository;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.commons.lang3.concurrent.CircuitBreakingException;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.*;
import static java.lang.Integer.parseInt;

@Transactional
public class GenericSteps {

    private List<Event> foundEvents;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    EventRepository eventRepo;

    @Given("^this list of ([a-zA-Z]+[a-zA-Z0-9_]*) :$")
    public void genericPersistListOfOneEntity(String entityName, DataTable table) throws ClassNotFoundException {
        var entities = createCollectionOfTransientEntitiesFromNameAndCucumberTable(entityName, table);
        entities.stream().forEach(entity -> entityManager.persist(entity));
    }

    @Then("^this last list of ([a-zA-Z]+[a-zA-Z0-9_]*) :$")
    public void genericCheckPersistedListOfOneEntity(String entityName, DataTable table) throws Exception {
        if (table.isEmpty())
            throw new CucumberException("Cucumber table is empty. This step should include a cucumber table where column header is a property.");
        Class<?> clazz = findClassPerNameCanBePlural(entityName);
        // Get real name for identifier
        var primaryField =getRecursivelySuperClassIdentifier(clazz);
        if(primaryField != null) {
            var sql = "SELECT o FROM " + clazz.getSimpleName() + " o ORDER BY " + primaryField.getName();
            Long countPersisted = (Long) entityManager.createQuery("SELECT COUNT(o) FROM " + clazz.getSimpleName() + " o").getSingleResult();
            // table.height() includes hearder row
            if (countPersisted < table.height() - 1) {
                throw new CucumberException("There are less row persisted (" + countPersisted + ") than rows in cucumber table (" + (table.height() - 1) + ").");
            }
            var persisted = entityManager.createQuery(sql).setFirstResult(countPersisted.intValue() - table.height() + 1).getResultList();
            assertMappedCucumberTableEqualEntityInstances(persisted, table.entries());
        }else{
            throw new CucumberException("Identifier for \""+clazz.getSimpleName()+"\" not found (no annotation @id).");
        }
    }

}
