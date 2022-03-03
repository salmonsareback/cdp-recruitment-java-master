package adeo.leroymerlin.cdp.cucumber.steps;

import com.sun.istack.NotNull;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;

public class CucumberUtils {
    // TODO fetch models package
    static String packageModelsPath = "adeo.leroymerlin.cdp.models";

    public static <T> void assertMappedCucumberTableEqualEntityInstances(List<T> instances, @NotNull List<Map<String, String>> mappedCucumberTable) throws Exception {
        assertMappedCucumberTableEqualEntityInstances(instances, mappedCucumberTable, "[blank]");
    }

    public static <T> void assertMappedCucumberTableEqualEntityInstances(List<T> instances, @NotNull List<Map<String, String>> mappedCucumberTable, String replaceWithEmptyString) {
        Class<?> entityClass = null;
        if (mappedCucumberTable.size() == 0) throw new CucumberException("Cucumber table should at least one row.");
        else if (instances.size() == 0) throw new CucumberException("No rows fetched to compare with Cucumber table.");
        else if (instances.size() != mappedCucumberTable.size())
            throw new CucumberException("Cucumber table expecting to compare " + mappedCucumberTable.size() + " rows but " + instances.size() + " were provided.");
        // For each row of the cucumber table
        int i = 0;
        for (Map<String, String> row : mappedCucumberTable) {
            try {
                assertOneInstanceEqualsOneMapOfFieldsValues(instances.get(i), row, replaceWithEmptyString);
            } catch (AssertionError | CucumberException e) {
                throw new CucumberException("Cucumber table at row " + (i + 1) + " :\r\n" + e.getMessage());
            }
            i++;
        }
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<String, String> cucumberTableRow, Class<T> entityClass) {
        return newEntityInstanceFromMap(cucumberTableRow, entityClass, "[blank]");
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<String, String> cucumberTableRow, Class<T> entityClass, String replaceWithEmptyString) {
        T instance;
        try {
            instance = entityClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CucumberException("Class " + entityClass + " is missing  null-arg constructor (no arguments).\r\n" + e.getMessage());
        }
        Class<?> refl = instance.getClass();
        var paramTypes = new Class[1];


        cucumberTableRow.forEach((cucumberFieldName, cucumberFieldValue) -> {
            // Check if fieldName is not empty
            if (cucumberFieldName == null || cucumberFieldName.length() == 0) {
                throw new CucumberException("Cucumber header is empty.");
            }
            Field fieldOfClass;
            try {
                fieldOfClass = getRecursivelySuperClassDeclaredField(refl, cucumberFieldName);
            } catch (NoSuchFieldException e) {
                fieldOfClass = null;
            }
            // try a second chance with first letter lowercase
            if (fieldOfClass == null) {
                cucumberFieldName = lowerCaseFirstLetter(cucumberFieldName);
                try {
                    fieldOfClass = getRecursivelySuperClassDeclaredField(refl, cucumberFieldName);
                } catch (NoSuchFieldException e2) {
                    fieldOfClass = null;
                }
            }
            if (fieldOfClass == null) {
                throw new CucumberException("Attribute \"" + cucumberFieldName + "\" does not exists for entity \"" + entityClass.getSimpleName() + "\".\r\n" +
                        "List of attributes : " + getRecursivelySuperClassDeclaredFields(refl).stream().map(Field::getName).collect(Collectors.joining(", ")));
            }
            try {
                var propertyDescriptor = new PropertyDescriptor(cucumberFieldName, refl);
                var instanceField = getRecursivelySuperClassDeclaredField(refl, cucumberFieldName);
                // Check if this property is annotated with @Id and @GeneratedValue in entity class
                if (Arrays.stream(instanceField.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 && Arrays.stream(instanceField.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
                    throw new CucumberException("Identifier \"" + cucumberFieldName + "\" is annotated @Id and @GeneratedValue. Values are unpredictable as being generated automatically. Such, cucumber table should not have header \"" + refl.getSimpleName() + "\".");
                }
                Method m = propertyDescriptor.getWriteMethod();
                // We suppose getter have 1 and only 1 parameter
                // get the actual param type
                var paramType0 = m.getParameterTypes()[0];
                paramTypes[0] = paramType0;
                if (cucumberFieldValue == null) {
                    m.invoke(instance, cucumberFieldValue);
                } else if (String.class.equals(paramType0)) {
                    if (cucumberFieldValue.equals(replaceWithEmptyString)) {
                        m.invoke(instance, "");
                    } else {
                        m.invoke(instance, cucumberFieldValue);
                    }
                } else if (int.class.equals(paramType0) || Integer.class.equals(paramType0)) {
                    m.invoke(instance, parseInt(cucumberFieldValue));
                } else if (Long.class.equals(paramType0)) {
                    m.invoke(instance, Long.parseLong(cucumberFieldValue));
                } else if (Double.class.equals(paramType0)) {
                    m.invoke(instance, Double.parseDouble(cucumberFieldValue));
                } else if (BigDecimal.class.equals(paramType0)) {
                    m.invoke(instance, new BigDecimal(cucumberFieldValue));
                } else if (byte.class.equals(paramType0) || Boolean.class.equals(paramType0)) {
                    m.invoke(instance, Boolean.valueOf(cucumberFieldValue));
                } else {
                    try {
                        // Whatever is the real type of attribute, it could happend that a setter with
                        // param as string exists. Thus, setter would convert the string to actual attribute type
                        paramTypes[0] = String.class;
                        var setWithParamString = refl.getMethod(m.getName(), paramTypes);
                        setWithParamString.invoke(instance, cucumberFieldValue);
                    } catch (IllegalArgumentException | InvocationTargetException e) {
                        throw new CucumberException("Cucumber could call setter for attribute \"" + cucumberFieldName + "\" with String value \"" + cucumberFieldValue + "\" but the error was returned :\r\n " + e.getCause() + "\r\n" + e.getMessage());
                    } catch (Exception e) {
                        throw new CucumberException("Cucumber failed to use setter for attribute \"" + cucumberFieldName + "\" with param type \"" + paramTypes[0].getName() + "\" for entity \"" + refl.getSimpleName() + "\".\r\n" + "==> Could be solved by overloading setter set" + upperCaseFirstLetter(cucumberFieldName) + " casting string to adequate attribute type. Example :\r\n" + "        public void setOneEnum(String oneEnum){\r\n" + "          this.oneEnum = TestEnum.valueOf(oneEnum);\r\n" + "        }\r\n");
                    }
                }
            } catch (CucumberException e) {
                throw e;
            } catch (IntrospectionException e) {
                throw new CucumberException("Cucumber table has the named column : \"" + cucumberFieldName + "\" ,\r\n" + "Setter and/or getter for attribute \"" + upperCaseFirstLetter(cucumberFieldName) + "\" is missing.\r\n");
            } catch (Exception ex) {
                throw new CucumberException("Cucumber table has the named column : \"" + cucumberFieldName + "\" ,\r\n" + "Setter for attribute \"" + upperCaseFirstLetter(cucumberFieldName) + "\" with param type " + paramTypes[0] + " throws " + ex.getClass().getName() + "\r\n" + ex.getMessage());
            }
        });
        return instance;
    }

    public static <T extends Object> Map<String, String> checkAndFormatMapForEntity(Map<String, String> cucumberTableRow, Class<T> entityClass) {
        return checkAndFormatMapForEntity(cucumberTableRow, entityClass, "[blank]");
    }

    public static <T extends Object> Map<String, String> checkAndFormatMapForEntity(Map<String, String> cucumberTableRow, Class<T> entityClass, String replaceWithEmptyString) {
//        T instance;
//        try {
//            instance = entityClass.getDeclaredConstructor().newInstance();
//        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
//            throw new CucumberException("Class " + entityClass + " is missing  null-arg constructor (no arguments).\r\n" + e.getMessage());
//        }
//        Class<?> refl = instance.getClass();
//        var paramTypes = new Class[1];

        var returned = new HashMap<String, String>();

        cucumberTableRow.forEach((cucumberFieldName, cucumberFieldValue) -> {
            // Check if fieldName is not empty
            if (cucumberFieldName == null || cucumberFieldName.length() == 0) {
                throw new CucumberException("Cucumber header is empty.");
            }
            Field fieldOfClass;
            try {
                fieldOfClass = getRecursivelySuperClassDeclaredField(entityClass, cucumberFieldName);
            } catch (NoSuchFieldException e) {
                fieldOfClass = null;
            }
            // try a second chance with first letter lowercase
            if (fieldOfClass == null) {
                cucumberFieldName = lowerCaseFirstLetter(cucumberFieldName);
                try {
                    fieldOfClass = getRecursivelySuperClassDeclaredField(entityClass, cucumberFieldName);
                } catch (NoSuchFieldException e2) {
                    fieldOfClass = null;
                }
            }
            if (fieldOfClass == null) {
                throw new CucumberException("Attribute \"" + cucumberFieldName + "\" does not exists for entity \"" + entityClass.getSimpleName() + "\".\r\n" +
                        "List of attributes : " + getRecursivelySuperClassDeclaredFields(entityClass).stream().map(Field::getName).collect(Collectors.joining(", ")));
            }
//            try {
//                var propertyDescriptor = new PropertyDescriptor(cucumberFieldName, refl);
            // Check if this property is annotated with @Id and @GeneratedValue in entity class
            if (Arrays.stream(fieldOfClass.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 && Arrays.stream(fieldOfClass.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
                throw new CucumberException("Identifier \"" + cucumberFieldName + "\" is annotated @Id and @GeneratedValue. Values are unpredictable as being generated automatically. Such, cucumber table should not have header \"" + entityClass.getSimpleName() + "\".");
            }
            // Check if replaceWithEmptyString
            if (cucumberFieldValue.trim().equals(replaceWithEmptyString)) {
                returned.put(cucumberFieldName, "\"\"");
            } else if (cucumberFieldValue == null) {
                returned.put(cucumberFieldName, "null");
            } else {
                // Check if the value should be quoted or not
                switch (fieldOfClass.getType().getSimpleName()) {
//                    switch (propertyDescriptor.getPropertyType().getSimpleName()) {
                    case "String", "char", "Enum", "Character":
                        returned.put(cucumberFieldName, "'" + cucumberFieldValue + "'");
                        break;
                    default:
                        returned.put(cucumberFieldName, cucumberFieldValue);
                }
            }
//               } catch (CucumberException e) {
//                throw e;
//            } catch (IntrospectionException e) {
//                throw new CucumberException("Cucumber table has the named column : \"" + cucumberFieldName + "\" ,\r\n" + "Setter and/or getter for attribute \"" + upperCaseFirstLetter(cucumberFieldName) + "\" is missing.\r\n");
//            } catch (NoSuchFieldException ex) {
//                throw new CucumberException("Cucumber table has the named column : \"" + cucumberFieldName + "\" ,\r\n" + "Setter for attribute \"" + upperCaseFirstLetter(cucumberFieldName) + "\" with param type " + paramTypes[0] + " throws " + ex.getClass().getName() + "\r\n" + ex.getMessage());
//            }
        });
        return returned;
    }

    public static String upperCaseFirstLetter(String s) {
        if (s.length() >= 2) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        } else if (s.length() == 1) {
            return s.substring(0, 1).toUpperCase();
        }
        return "";
    }

    private static String lowerCaseFirstLetter(String s) {
        if (s.length() >= 2) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        } else if (s.length() == 1) {
            return s.substring(0, 1).toLowerCase();
        }
        return "";
    }

    public static List<?> createCollectionOfTransientEntitiesFromNameAndCucumberTable(String entityName, DataTable table) {
        Class<?> type = findClassPerNameCanBePlural(entityName);
        AtomicInteger rowNb = new AtomicInteger(0);
        List<?> entities = table.asMaps(String.class, String.class).stream().map(entry -> {
            try {
                rowNb.getAndIncrement();
                return newEntityInstanceFromMap(entry, type);
            } catch (AssertionError | CucumberException e) {
                throw new CucumberException("Cucumber table at row " + rowNb + " :\r\n" + e.getMessage());
            }
        }).collect(Collectors.toList());
        return entities;
    }

    public static Class<?> findClassPerNameCanBePlural(String entityName) {
        // An entity class name is reputed to start with an uppercase letter
        final String s = upperCaseFirstLetter(entityName);
        List<String> potentialClasses = Arrays.asList(s, // Uppercase first letter
                s.substring(0, entityName.length() - 1) // Uppercase first letter and delete potential ending s
        );
        final String[] entityNameSingular = new String[1];
        potentialClasses.stream().forEach(name -> {
            try {
                Class.forName(packageModelsPath + "." + name);
                entityNameSingular[0] = name;
            } catch (Exception e) {
            }
        });
        if (entityNameSingular[0] == null)
            throw new CucumberException("No entity class exists for \"" + packageModelsPath + "." + entityName + "\" (plural authorized).");
        try {
            Class<?> type = Class.forName(packageModelsPath + "." + entityNameSingular[0]);
            return type;
        } catch (Exception e) {
            throw new CucumberException("No entity class exists for \"" + entityName + "\" (plural authorized).");
        }
    }

    private static <T extends Object> void assertOneInstanceEqualsOneMapOfFieldsValues(T instance, Map<String, String> row) throws AssertionError {
        assertOneInstanceEqualsOneMapOfFieldsValues(instance, row, "[blank]");
    }

    private static <T extends Object> void assertOneInstanceEqualsOneMapOfFieldsValues(T instance, Map<String, String> row, String replaceWithEmptyString) throws AssertionError {
        Class refl = instance.getClass();
        // For each field of the map
        row.forEach((cucumberFieldName, cucumberFieldValue) -> {
            // Check if fieldName is not empty
            if (cucumberFieldName == null || cucumberFieldName.length() == 0) {
                throw new CucumberException("Cucumber header is empty.");
            }
            // Check if cucumber table value is empty string
            if (replaceWithEmptyString != null && replaceWithEmptyString.length() > 0 && cucumberFieldValue != null && cucumberFieldValue.toString().trim().equals(replaceWithEmptyString.trim())) {
                cucumberFieldValue = "";
            }
            Object gettedValueOfInstance = null;
            // Check if cucumber field name exists on instance class
            Field equivalentFieldInInstance = null;
            try {
                equivalentFieldInInstance = getRecursivelySuperClassDeclaredField(refl, cucumberFieldName);
            } catch (NoSuchFieldException e) {
                equivalentFieldInInstance = null;
            }
            // Have a second try by un-capitalizing first letter
            if (equivalentFieldInInstance == null) {
                cucumberFieldName = lowerCaseFirstLetter(cucumberFieldName);
                try {
                    equivalentFieldInInstance = getRecursivelySuperClassDeclaredField(refl, cucumberFieldName);
                } catch (NoSuchFieldException e2) {
                    equivalentFieldInInstance = null;
                }
                if (equivalentFieldInInstance == null) {
                    throw new CucumberException("Field name \"" + cucumberFieldName + "\" does not exist for entity \"" + refl.getSimpleName() + "\".\r\n" + "List of attributes is " + getRecursivelySuperClassDeclaredFields(refl).stream().map(Field::getName).collect(Collectors.joining(", ")));

                }
            }
            // Check if this property is annotated with @Id and @GeneratedValue in entity class
            if (Arrays.stream(equivalentFieldInInstance.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 && Arrays.stream(equivalentFieldInInstance.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
                throw new CucumberException("Identifier \"" + cucumberFieldName + "\" is annotated @Id and @GeneratedValue. Values are unpredictable as being generated automatically. Such, cucumber table should not have header \"" + cucumberFieldName + "\".");
            }
            // Find getter
            PropertyDescriptor propertyDescriptor = null;
            try {
                propertyDescriptor = new PropertyDescriptor(cucumberFieldName, refl);
            } catch (IntrospectionException e) {
                throw new CucumberException("Fail to build property for field  \\\"\" + cucumberFieldName + \"\\\" not found for class \"" + refl.getSimpleName() + "\".");
            }
            // A bean entity is reputed to have only one getter
            Method getter = propertyDescriptor.getReadMethod();
            if (getter == null) {
                throw new CucumberException("Getter for field \"" + cucumberFieldName + "\" not found for class " + refl.getSimpleName() + ".");
            }
            try {
                gettedValueOfInstance = getter.invoke(instance, new Class[0]);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new CucumberException("Trying to get value for \"" + cucumberFieldName + "\" failed with error :\r\n" + e.getMessage());
            }

            // Try to compare with different numeric and date format
            // because toString() comparison would fail such 50 not equals to 50.0000
            try {
                if (cucumberFieldValue == null) {
                    assertThat(gettedValueOfInstance).isNull();
                } else {
                    switch (getter.getReturnType().getSimpleName()) {
                        case "Long":
                            assertThat(gettedValueOfInstance).isEqualTo(Long.parseLong(cucumberFieldValue));
                            break;
                        case "Integer":
                            assertThat(gettedValueOfInstance).isEqualTo(Integer.parseInt(cucumberFieldValue));
                            break;
                        case "Double":
                            assertThat(gettedValueOfInstance).isEqualTo(Double.parseDouble(cucumberFieldValue));
                            break;
                        case "Boolean":
                            assertThat(gettedValueOfInstance).isEqualTo(Boolean.parseBoolean(cucumberFieldValue));
                            break;
                        case "BigDecimal":
                            assertThat(gettedValueOfInstance).isEqualTo(new BigDecimal(cucumberFieldValue));
                            break;
                        case "LocalDate":
                            assertThat(gettedValueOfInstance).isEqualTo(LocalDate.parse(cucumberFieldValue));
                        default:
                            if (gettedValueOfInstance == null) {
                                assertThat(gettedValueOfInstance).isEqualTo(cucumberFieldValue.toString());
                            } else {
                                assertThat(gettedValueOfInstance.toString()).isEqualTo(cucumberFieldValue.toString());
                            }
                    }

                }
            } catch (Exception e) {
                throw new CucumberException("Assertion error at column \"" + cucumberFieldName + "\"\n\r" + e.getMessage());
            }
        }); //row.forEach
    }

    //     List of fields including those inherited
    static List<Field> getRecursivelySuperClassDeclaredFields(Class clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Field> result = new ArrayList<>(getRecursivelySuperClassDeclaredFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields())
                // TODO ligne suivante desactivée pour public void genericCheckPersistedListOfOneEntity
                // Mais ne pose pas de soucis dans les méthode de cette classe ??
                // Mais doit-on vraiment filtrer les attributs protected et private dans le cadre des tests ??
//                .filter(f -> Modifier.isPublic(f.getModifiers()) || Modifier.isProtected(f.getModifiers()))
                .collect(Collectors.toList());
        result.addAll(filteredFields);
        return result;
    }

    // Return a field matching string including those inherited
    public static Field getRecursivelySuperClassDeclaredField(Class clazz, String fieldName) throws NoSuchFieldException {
        Field result = null;
        if (clazz != null) {
            try {
                result = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                result = getRecursivelySuperClassDeclaredField(clazz.getSuperclass(), fieldName);
//                if (result != null)
                return result;
            }
        } else {
            return null;
        }
        // Retry to generate not catched assertion error
        return clazz.getDeclaredField(fieldName);
    }

    public static Field getRecursivelySuperClassIdentifier(Class clazz) {
        return getRecursivelySuperClassDeclaredFields(clazz).stream().filter(field -> Arrays.stream(field.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0).findFirst().orElse(null);
    }
}
