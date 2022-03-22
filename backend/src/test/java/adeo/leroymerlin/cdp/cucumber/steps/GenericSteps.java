package adeo.leroymerlin.cdp.cucumber.steps;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.*;


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
    public void genericPersistListOfOneEntityWithRelation2(String entityName, DataTable table) {
        if (table.isEmpty())
            throw new CucumberException("Cucumber table is empty. This step should include a cucumber table where column header is a property.");
        // If cucumberFieldName patterned xxxx.yyy, consider as a relation (OneToOne, ManyToOne, ...)
        // For those columns, find the instance in relation
        var entityType = findEntityTypePerNameThatCanHavePlural(entityName, entityManager);
        List<Map<Attribute<?, ?>, Object>> tableWithRelation = findAttributesAndAssertRelations(table, entityType);
        // Now, cucumber header and relations have been checked
        var entities = createListOfEntitiesFromListOfMapAttributesAndRelation(entityType, tableWithRelation);
        AtomicInteger row = new AtomicInteger(0);
        entities.stream().forEach(entity -> {
            row.getAndIncrement();
            try {
                entityManager.persist(entity);
            } catch (PersistenceException e) {
                throw new CucumberException("Row " + row + ", some information seems missing to persist the information.\r\n" +
                        "Check every mandatory (non-null) attribute is listed in table : \r\n", e);
            }
            // Let chaining with 'And related to its otherEntity' by memorizing last entity
            lastEntity = entity;
        });
    }

    /**
     * @param table : a cucumber table with headers as properties of entity
     *              | title            | NbStars | Comment    |   Manager.Name |
     *              | Jazz in Lille    | 5       |  [blank]   |   Peter        |
     *              | Hard rock folies | 1       | So noisy ! |   Max          |
     *              | Old melodies     |         | For aged   |   Peter        |
     * @return a table where pattern such as Mananger.Name have been interpreted as
     * a relation with entity Manager.
     * A persisted instance of Manager with attribute name will be fetched
     * If Manager.Name is not unique, the first record would be sent
     */
    private List<Map<Attribute<?, ?>, Object>> findAttributesAndAssertRelations(DataTable table, EntityType<?> entityType) {
        // If cucumberFieldName patterned xxxx.yyy, consider as a relation (OneToOne, ManyToOne, ...)
        // For those columns, find the instance in relation
        Pattern pattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9_]+)\\.([a-zA-Z][a-zA-Z0-9_]+)");
        AtomicInteger rowNb = new AtomicInteger(0);
        // List of metadata attributes of the entity type

        // Rebuilt the table by detecting relation and getting the related entity
        List<Map<Attribute<?, ?>, Object>> tableWithRelation = table.asMaps(String.class, String.class).stream().map(entry -> {
            rowNb.getAndIncrement();
            // Initiate output format for one row (entry) of table
            Map<Attribute<?, ?>, Object> attributesWithRelations = new HashMap<>();
            entry.forEach((cucumberFieldName, cucumberFieldValue) -> {
                // First consider value as is, a string from cucumber table
                Object value = cucumberFieldValue;
                Matcher matcher = pattern.matcher(cucumberFieldName);
                // Does the column name match the pattern xxxxx.yyyyy ?
                if (matcher.find()) {
                    // The pattern is attributeNameOfAsAnotherEntity.oneOfItsFields (matcher.group(1).matcher.group(2))
                    // Does the related class exists ?
                    // matcher.group(1) is the name of attribute in entityType, we could find the class of the attribute through Field
                    // Method to search through class : fieldOfRelation = getRecursivelySuperClassDeclaredField(entityType.getClass(), matcher.group(1));
                    // But let use the metadata model
                    Attribute<?, ?> attributeOfColumnName = getAttributeFromName(entityType, matcher.group(1));

                    // Test if the relation exists with criteria
                    /// Is the value not null
                    //TODO such, any null value means this attribute is not a criteria, what if the test may check the value is null ??
                    if (cucumberFieldValue != null && !cucumberFieldValue.isBlank()) {
                        var entityTypeRelation = findEntityTypePerNameThatCanHavePlural(attributeOfColumnName.getJavaType().getSimpleName(), entityManager);
                        Attribute<?, ?> attributeOfRelationEntity = getAttributeFromName(entityTypeRelation, matcher.group(2));
                        Map<Attribute<?, ?>, Object> criteriaForEntity = Map.of(attributeOfRelationEntity, cucumberFieldValue);
                        var entityCriterias = checkAndFormatMapForEntity(criteriaForEntity, entityTypeRelation);
                        value = this.findPersistedWithCriteriasAndRelations(entityCriterias, entityTypeRelation);
                        if (value == null) {
                            throw new CucumberException("Row " + rowNb + " , no " + attributeOfColumnName.getJavaType() + " with " + matcher.group(2) + " matching \"" + cucumberFieldValue + "\".");
                        }
                        attributesWithRelations.put(attributeOfColumnName, value);
                    } // if value is not null
                } else {
                    //TODO getAttribute could be searched from the top, and use getPersistentAttributeType() to check if BASIC, ELEMENT_COLLECTION, MANY_TO_MANY ...
                    attributesWithRelations.put(getAttributeFromName(entityType, cucumberFieldName), cucumberFieldValue);
                } //elseif matcher find
            });//entry.foreach
            return attributesWithRelations;
        }).collect(Collectors.toList());
        return tableWithRelation;
    }

    private Attribute<?, ?> getAttributeFromName(EntityType<?> entityType, String columnName) {
        Attribute<?, ?> attributeOfColumnName = null;
        try {
            attributeOfColumnName = entityType.getAttribute(columnName);
        } catch (IllegalArgumentException e) {
            try {
                attributeOfColumnName = entityType.getAttribute(invertCaseFirstLetter(columnName));
            } catch (IllegalArgumentException e2) {
                Set<Attribute<?, ?>> attributes = (Set<Attribute<?, ?>>) entityType.getAttributes();
                throw new CucumberException("Cannot find attribute \"" + columnName + "\" for " + entityType.getName() + "\r\n" +
                        "Existing attributes are : " + attributes.stream().map(a -> a.getName()).collect(Collectors.joining(", ")), e);
            }
        }
        return attributeOfColumnName;
    }


    @Then("^this last list of ([a-zA-Z]+[a-zA-Z0-9_]*) :$")
    public void genericCheckPersistedListOfOneEntity(String entityName, DataTable table) throws Exception {
        if (table.isEmpty())
            throw new CucumberException("Cucumber table is empty. This step should include a cucumber table where column header is a property.");
        Class<?> clazz = findClassPerNameCanBePlural(entityName);
        // Get real name for identifier
        var primaryField = getRecursivelySuperClassFieldIdentifier(clazz);
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
    @And("this later having (finally |)(a |an |)([a-zA-Z]+[a-zA-Z0-9_]*)[s]? :$")
    @Transactional
    public void thisLaterHavingRelationalEntities(String finalRelation, String verbose, String entityName, DataTable table) {
        if (table.isEmpty())
            throw new CucumberException("Cucumber table is empty. This step should include a cucumber table where column header is a property.");
        // Check if last entity of previous cucumber step is related to this entity
        var entityTypeToLinkToPrevious = findEntityTypePerNameThatCanHavePlural(entityName, entityManager);
        var previousEntityType = findEntityTypePerNameThatCanHavePlural(lastEntity.getClass().getName(), entityManager);
//        ===================
        Attribute<?, ?> previousAttribute;
        try {
            previousAttribute = previousEntityType.getAttribute(entityTypeToLinkToPrevious.getName());
        } catch (Exception e) {
            throw new CucumberException("Entity \"" + lastEntity.getClass().getSimpleName() + "\" of previous step has no relation with entity\" " + entityTypeToLinkToPrevious.getName() + "\".", e);
        }
        //TODO could also check             previousAttribute.isAssociation()
        // Remap Cucumber table
        List<Map<Attribute<?, ?>, Object>> tableWithRelation = findAttributesAndAssertRelations(table, entityTypeToLinkToPrevious);

        Object matching = new Object();

        for (Map<Attribute<?, ?>, Object> entry : tableWithRelation) {
            // Persist entity if not exists
            var entityCriterias = checkAndFormatMapForEntity(entry, entityTypeToLinkToPrevious);
            matching = this.findPersistedWithCriteriasAndRelations(entityCriterias, entityTypeToLinkToPrevious);
            if (matching == null) {
                // Create it
                matching = newEntityInstanceFromMap(entry, entityTypeToLinkToPrevious);
                entityManager.persist(matching);
            }

            // Link with multiple relations

            if (previousAttribute.isCollection()) {
                //    Make a try with method addEntity, ex addBands for a Set<Band>
                var paramTypes = new Class[1];
                paramTypes[0] = previousAttribute.getJavaType();
                Method addRelation = null;
                try {
                    addRelation = lastEntity.getClass().getDeclaredMethod(upperCaseFirstLetter(previousAttribute.getName()), paramTypes);
                    addRelation.invoke(lastEntity, matching);
                    entityManager.merge(lastEntity);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CucumberException("Cucumber failed to invoke " + addRelation.getName() + " method on entity \"" + lastEntity.getClass().getSimpleName() + "\".");
                } catch (NoSuchMethodException e) {
                    // Make a second try with setEntities
                    Method getRelation;
                    try {
                        getRelation = new PropertyDescriptor(upperCaseFirstLetter(previousAttribute.getName()), lastEntity.getClass()).getReadMethod();
                    } catch (IntrospectionException ex) {
                        throw new CucumberException("Cucumber could not find getter of \"" + lastEntity.getClass().getSimpleName() + "\" " + "for relation (@ManyToOne or @ManyToMany) to \"" + previousAttribute.getName() + "\".");
                    }
                    Method setRelation;
                    try {
                        setRelation = new PropertyDescriptor(previousAttribute.getName(), lastEntity.getClass()).getWriteMethod();
                    } catch (IntrospectionException ex) {
                        throw new CucumberException("Cucumber could not find setter of \"" + lastEntity.getClass().getSimpleName() + "\" " + "for relation (@ManyToOne or @ManyToMany) to \"" + previousAttribute.getName() + "\".");
                    }
                    Collection related = null;
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
                    setRelation = new PropertyDescriptor(previousAttribute.getName(), lastEntity.getClass()).getWriteMethod();
                } catch (IntrospectionException e) {
                    throw new CucumberException("Cucumber could not find setter of \"" + lastEntity.getClass().getSimpleName() + "\" " + "for relation (@OneToMany or @OneToOne) to \"" + previousAttribute.getName() + "\".");

                }
                try {
                    setRelation.invoke(lastEntity, matching);
                    entityManager.merge(lastEntity);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CucumberException("Cucumber failed to invoke " + setRelation.getName() + " method on entity \"" + lastEntity.getClass().getSimpleName() + "\".");
                }
            }
        } //for

        // Let chain with 'And related to its otherEntity' by memorizing last entity
        if (!finalRelation.equals("finally ")) {
            // Fetch the full object to force lazy relations and
            // to avoid exception at next step : Failed to lazily initialize a collection of role could not initialize ...
            System.out.println("Refresh new matching");
            System.out.println(matching);
            Field fieldId = getRecursivelySuperClassFieldIdentifier(matching.getClass());
            System.out.println(fieldId);
//            try {
//                Method getId = matching.getClass().getMethod("get" + upperCaseFirstLetter(fieldId.getName()), null);
//                Object id = getId.invoke(matching, null);
//                System.out.println(id);
//                lastEntity = entityManager.find(entityClass, id);
//                // Find each relation
//                for (Field field : getRecursivelySuperClassDeclaredFields(entityClass)) {
//                    // Check fetching type relation by relation type
//                    try {
//                        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
//                        if (oneToMany != null && oneToMany.fetch().equals(FetchType.LAZY))
//                            new PropertyDescriptor(field.getName(), entityClass).getReadMethod().invoke(lastEntity);
//
//                        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
//                        if (manyToMany != null && manyToMany.fetch().equals(FetchType.LAZY))
//                            new PropertyDescriptor(field.getName(), entityClass).getReadMethod().invoke(lastEntity);
//
//                        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
//                        if (manyToOne != null && manyToOne.fetch().equals(FetchType.LAZY))
//                            new PropertyDescriptor(field.getName(), entityClass).getReadMethod().invoke(lastEntity);
//
//                    } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
//                        throw new CucumberException("Cucumber could not find getter for \"" + field.getName() + "\"\r\n", e);
//                    }
//
//                }

//                System.out.println(lastEntity);

//            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//                System.out.println("Fail to refresh");
//                System.out.println(matching);
//                lastEntity = matching;
//            }

        }

        //Clean out
        entityManager.flush();
    }

    /**
     * This sentence is used to assert the existence of some entities with some criteria
     * It is followed by a table where each column header is a property name
     * Example :
     * And some events exist :
     * #      | Title            | NbStars | Comment    |
     * #      | Jazz in Lille    | 5       |            |
     * #      | Hard rock folies | 1       | So noisy ! |
     * #      | Old melodies     |         | For aged   |
     * #      | Pop 80th         | 3       | [blank]    |
     * <p>
     * The only request is that the Entity class exists
     * with properties and getter/setter for name
     * Note :
     * Capitalizing property in cucumber table is tolerated
     * [blank] stands for empty string where nothing stands for null
     *
     * @author Loïc Ducatillon
     */
    @Then("^some ([a-zA-Z]+[a-zA-Z0-9_]*)[s]? exist :$")
    public void someEntityExistsWithRelation(String entityName, DataTable table) {
        if (table.isEmpty())
            throw new CucumberException("Cucumber table is empty. This step should include a cucumber table where column header is a property.");
        // If cucumberFieldName patterned xxxx.yyy, consider as a relation (OneToOne, ManyToOne, ...)
        // For those columns, find the instance in relation
        var entityType = findEntityTypePerNameThatCanHavePlural(entityName, entityManager);
        List<Map<Attribute<?, ?>, Object>> tableWithRelation = findAttributesAndAssertRelations(table, entityType);

        // Now tableWithRelation has values replaced by entities
        Class<?> clazz = findClassPerNameCanBePlural(entityName);
        AtomicInteger row = new AtomicInteger(0);
        tableWithRelation.stream().forEach(entry -> {
            row.getAndIncrement();
            // Check if entity exists
            var entityCriterias = checkAndFormatMapForEntity(entry, entityType);

            Object matching = this.findPersistedWithCriteriasAndRelations(entityCriterias, entityType);
            if (matching == null) {
                throw new CucumberException("Row " + row + ", no " + entityName + " matching whole those criteria.");
            }
        });

        // TODO store recursively if only one
    }


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
//    @And("^it[s]? related ([1|one|only one|some|exactly [0-9]*) ([a-zA-Z]+[a-zA-Z0-9_]*)[s]? :$")
//    public void TODO_1() {
//// TODO store recursively if only one
//    }
    private Object findPersistedWithCriterias(Map<String, Object> entityCriterias, Class<?> clazz) {
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

    private <clazz> Object findPersistedWithCriteriasAndRelations(Map<Attribute<?, ?>, Object> entityCriterias, EntityType<?> entityType) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        Class entityTypeClass = entityType.getJavaType();
        CriteriaQuery<clazz> criteriaQuery = criteriaBuilder.createQuery(entityTypeClass);
        Root<clazz> root = criteriaQuery.from(entityTypeClass);
        List<Predicate> andPredicates = new ArrayList<>();
        for (var criteria : entityCriterias.entrySet()) {
            var attribute = criteria.getKey();
            if (attribute.isAssociation()) {
                Join<Object, Object> relation = root.join(attribute.getName());
                andPredicates.add(criteriaBuilder.equal(relation, criteria.getValue()));
            } else {
//                ParameterExpression<String> paramCriteria =criteriaBuilder.parameter(String.class);
//                criteriaQuery.where(criteriaBuilder.equal(root.get(field.getName()),paramCriteria));
                andPredicates.add(criteriaBuilder.equal(root.get(attribute.getName()), criteria.getValue()));
            }
        }

        criteriaQuery.where(criteriaBuilder.and(andPredicates.toArray(Predicate[]::new)));
        var matchings = entityManager.createQuery(criteriaQuery).getResultList();
        // TODO what to do if several results ??
        if (matchings.size() >= 1) return matchings.get(0);
        return null;
    }

    private boolean isFieldAnEntity(Field field) {
        java.util.function.Predicate<EntityType> isEqualToFieldClass = entityType -> entityType.getJavaType().equals(field.getType());
        var firstEntity = entityManager.getMetamodel().getEntities().stream().filter(isEqualToFieldClass).findFirst();
        return firstEntity.isPresent();
    }
}


