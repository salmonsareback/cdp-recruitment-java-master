package adeo.leroymerlin.cdp.cucumber.steps;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.*;
import static org.apache.commons.lang3.ArrayUtils.lastIndexOf;

@Transactional
public class GenericSteps {

    private Object lastEntity;

    @PersistenceContext
    EntityManager entityManager;

    /**
     * This sentence is used to build and persist a list instance of one entity
     * It is followed by a table where each column header is a property name
     * Example :
     * Given this list of events :
     * | title            | NbStars | Comment    |
     * | Jazz in Lille    | 5       |  [blank]   |
     * | Hard rock folies | 1       | So noisy ! |
     * | Old melodies     |         | For aged   |
     * <p>
     * The only request is that an Entity class Event exists
     * with properties and getter/setter for title, nbStars, comment
     * Note :
     * Capitalizing property in cucumber table is tolerated
     * [blank] stands for empty string where nothing stands for null
     *
     * @author Loïc Ducatillon
     */
    @Given("^this list of ([a-zA-Z]+[a-zA-Z0-9_]*) :$")
    public void genericPersistListOfOneEntity(String entityName, DataTable table) {
        var entities = createCollectionOfTransientEntitiesFromNameAndCucumberTable(entityName, table);
        entities.stream().forEach(entity -> {
            entityManager.persist(entity);
            // Let chaining with 'And related to its otherEntity' by memorizing last entity
            lastEntity = entity;
        });
    }

    @Then("^this last list of ([a-zA-Z]+[a-zA-Z0-9_]*) :$")
    public void genericCheckPersistedListOfOneEntity(String entityName, DataTable table) throws Exception {
        if (table.isEmpty())
            throw new CucumberException("Cucumber table is empty. This step should include a cucumber table where column header is a property.");
        Class<?> clazz = findClassPerNameCanBePlural(entityName);
        // Get real name for identifier
        var primaryField = getRecursivelySuperClassIdentifier(clazz);
        if (primaryField != null) {
            var sql = "SELECT o FROM " + clazz.getSimpleName() + " o ORDER BY " + primaryField.getName();
            Long countPersisted = (Long) entityManager.createQuery("SELECT COUNT(o) FROM " + clazz.getSimpleName() + " o").getSingleResult();
            // table.height() includes hearder row
            if (countPersisted < table.height() - 1) {
                throw new CucumberException("There are less row persisted (" + countPersisted + ") than rows in cucumber table (" + (table.height() - 1) + ").");
            }
            var persisted = entityManager.createQuery(sql).setFirstResult(countPersisted.intValue() - table.height() + 1).getResultList();
            assertMappedCucumberTableEqualEntityInstances(persisted, table.entries());
        } else {
            throw new CucumberException("Identifier for \"" + clazz.getSimpleName() + "\" not found (no annotation @id).");
        }
    }

    /**
     * This sentence is used to build and persist a relationed list instance of one entity
     * It is related to the last row of this list of parent entity
     * It is followed by a table where each column header is a property name
     * Example :
     * And this later having bands :
     * | name             |
     * | Charly           |
     * | Petter           |
     * <p>
     * The only request is that another Entity class Band exists
     * and is related to Event (one or multiple relation)
     * with properties and getter/setter for name
     * Note :
     * Capitalizing property in cucumber table is tolerated
     * [blank] stands for empty string where nothing stands for null
     * The related entities (band in the example) would be created if not exists
     * And relation would be settled
     *
     * @author Loïc Ducatillon
     */
    @And("this later having [a |an ]?([a-zA-Z]+[a-zA-Z0-9_]*)[s]? :$")
    public void thisLaterHavingRelationalEntities(String entityName, DataTable table) {
        // Check if previous entity is related to this entity
        Class<?> entityClass = findClassPerNameCanBePlural(entityName);
        var fieldsRelatedEntity = getRecursivelySuperClassDeclaredFields(lastEntity.getClass());
        Field relatedField = fieldsRelatedEntity.stream().filter(field -> field.getGenericType().toString().contains(entityClass.getName())).findFirst().orElse(null);
        if (relatedField == null)
            throw new CucumberException("Entity \"" + lastEntity.getClass().getSimpleName() + "\" has no relation with entity\" " + entityClass.getSimpleName() + "\".");
//        AtomicReference<Object> lastMatching = null;
        table.asMaps(String.class, String.class).stream().forEach(entry -> {
            // Persist entity if not exists
            var entityCriterias = checkAndFormatMapForEntity(entry, entityClass);
            Object matching = this.findPersistedWithCriterias(entityCriterias, entityClass);
            if (matching == null) {
                // Create it
                matching = newEntityInstanceFromMap(entry, entityClass);
                entityManager.persist(matching);
            }
//            lastMatching.set(matching);

            // Link with multiple relations
            Class initClass = relatedField.getType();

            if (initClass.equals(Set.class) || initClass.equals(List.class) || initClass.equals(Arrays.class)) {
                //    Make a try with method addEntity, ex addBands for a Set<Band>
                var paramTypes = new Class[1];
                paramTypes[0] = initClass;
                Method addRelation = null;
                try {
                    addRelation = lastEntity.getClass().getDeclaredMethod(upperCaseFirstLetter(relatedField.getName()), paramTypes);
                    addRelation.invoke(lastEntity, matching);
                    entityManager.merge(lastEntity);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CucumberException("Cucumber failed to invoke " + addRelation.getName() + " method on entity \"" + lastEntity.getClass().getSimpleName() + "\".");
                } catch (NoSuchMethodException e) {
                    // Make a second try with setEntities
                    Method getRelation;
                    try {
                        getRelation = new PropertyDescriptor(upperCaseFirstLetter(relatedField.getName()), lastEntity.getClass()).getReadMethod();
                    } catch (IntrospectionException ex) {
                        throw new CucumberException("Cucumber could not find getter of \"" + lastEntity.getClass().getSimpleName() + "\" " + "for relation (@ManyToOne or @ManyToMany) to \"" + initClass.getSimpleName() + "\".");
                    }
                    Method setRelation;
                    try {
                        setRelation = new PropertyDescriptor(relatedField.getName(), lastEntity.getClass()).getWriteMethod();
                    } catch (IntrospectionException ex) {
                        throw new CucumberException("Cucumber could not find setter of \"" + lastEntity.getClass().getSimpleName() + "\" " + "for relation (@ManyToOne or @ManyToMany) to \"" + relatedField.getName() + "\".");
                    }
                    Collection related;
                    try {
                        related = (Collection) getRelation.invoke(lastEntity, new Class[0]);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new CucumberException("Cucumber failed to invoke " + getRelation.getName() + " method on entity \"" + lastEntity.getClass().getSimpleName() + "\".");
                    }
                    try {
                        related.add(matching);
                        setRelation.invoke(lastEntity, related);
                        entityManager.merge(lastEntity);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new CucumberException("Cucumber failed to invoke " + setRelation.getName() + " method on entity \"" + lastEntity.getClass().getSimpleName() + "\".");
                    }
                }
            } else {
                // This would be a single relation (@oneToMany or @OneToOne)
                Method setRelation;
                try {
                    setRelation = new PropertyDescriptor(relatedField.getName(), lastEntity.getClass()).getWriteMethod();
                } catch (IntrospectionException e) {
                    throw new CucumberException("Cucumber could not find setter of \"" + lastEntity.getClass().getSimpleName() + "\" " + "for relation (@OneToMany or @OneToOne) to \"" + relatedField.getName() + "\".");

                }
                try {
                    setRelation.invoke(lastEntity, matching);
                    entityManager.merge(lastEntity);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CucumberException("Cucumber failed to invoke " + setRelation.getName() + " method on entity \"" + lastEntity.getClass().getSimpleName() + "\".");
                }
            }

        });
            // Let chaining with 'And related to its otherEntity' by memorizing last entity
//     TODO store recursively for the last Given  without using stop propagation
//            lastEntity = lastMatching;

        //Clean out
        entityManager.flush();
    }

//    @And("^it[s]? related ([1|one|only one|some|exactly [0-9]*) ([a-zA-Z]+[a-zA-Z0-9_]*)[s]? :$")
//    public void TODO_1 (){
// TODO store recursively if only one

    /**
     * This sentence is used to assert a relationed list instance of one entity
     * It is related to the last row of this list of parent entity
     * It is followed by a table where each column header is a property name
     * Example :
     * And its related exactly 3 bands :
     * | name             |
     * | Charly           |
     * | Petter           |
     * <p>
     * The only request is that another Entity class Band exists
     * and is related to Event (one or multiple relation)
     * with properties and getter/setter for name
     * Using 'only one' or 'exactly x' will also assert the number of related entities
     * Note :
     * Capitalizing property in cucumber table is tolerated
     * [blank] stands for empty string where nothing stands for null
     * The related entities (band in the example) would be created if not exists
     * And relation would be settled
     *
     * @author Loïc Ducatillon
     */
//    }
    private Object findPersistedWithCriterias(Map<String, String> entityCriterias, Class<?> clazz) {
        String sql = "FROM " + clazz.getSimpleName() + " e WHERE ";
        String where = "";
        for (var criteria : entityCriterias.entrySet()) {
            where = where + " AND e." + criteria.getKey() + " = " + criteria.getValue();
        }
        sql = sql + where.substring(5);
        var matchings = entityManager.createQuery(sql).getResultList();
        // TODO what to do if several results ??
        if (matchings.size() >= 1) return matchings.get(0);
        return null;
    }
}
