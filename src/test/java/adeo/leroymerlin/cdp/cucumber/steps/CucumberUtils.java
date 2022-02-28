package adeo.leroymerlin.cdp.cucumber.steps;

import com.sun.istack.NotNull;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

public class CucumberUtils {
    // TODO fetch models package
    static String packageModelsPath = "adeo.leroymerlin.cdp.models";

    public static <T> void assertMappedCucumberTableEqualEntityInstances(List<T> instances, @NotNull List<Map<String, String>> mappedCucumberTable) throws Exception {
        assertMappedCucumberTableEqualEntityInstances(instances, mappedCucumberTable, "[blank]");
    }

    public static <T> void assertMappedCucumberTableEqualEntityInstances(List<T> instances, @NotNull List<Map<String, String>> mappedCucumberTable, String replaceWithEmptyString) throws Exception {
        Class<?> entityClass = null;
        if (mappedCucumberTable.size() == 0)
            throw new CucumberException("Cucumber table should at least one row.");
        else if (instances.size() == 0)
            throw new CucumberException("No rows fetched to compare with Cucumber table.");
        else if (instances.size() != mappedCucumberTable.size())
            throw new CucumberException("Cucumber table expecting to compare " + mappedCucumberTable.size() + " rows but " + instances.size() + " were provided.");


//        else entityClass = instances.get(0).getClass();
//        var entityFields = new EntityFields(entityClass);
        int i = 0;


        // For each row of the cucumber table
        for (Map<String, String> row : mappedCucumberTable) {
            try {
                assertOneInstanceEqualsOneMapOfFieldsValues(instances.get(i), row, replaceWithEmptyString);
            } catch (AssertionError | CucumberException e) {
                throw new CucumberException("Cucumber row " + (i + 1) + " :\r\n" + e.getMessage());
            }
//            Class refl = instances.get(i).getClass();
//            Class[] paramTypes = new Class[0];
//            // For each column of one line of cucumber table , the name of the column is the key
//            for (Map.Entry cucumberMapColumnNameAndValue : row.entrySet()) {
//                String columnName = cucumberMapColumnNameAndValue.getKey().toString();
//
//                if (columnName.equalsIgnoreCase(entityFields.getPrimaryKey())) {
//                    throw new CucumberException("Identifier \"" + columnName + "\" values should not be compared because identifier values could not be generated manually. Such, cucumber table should not have header \"" + columnName + "\".");
//                } else {
//                    // Find corresponding entityFields
//                    EntityFields.FieldDef matchingField = entityFields.matching(columnName);
//                    if (matchingField == null) {
//                        throw new CucumberException("Attribute \"" + columnName + "\" not found for class " + instances.get(0).getClass().getName() + ". Entity attributes are : " + entityFields.fieldsList() + ".");
//                    } else {
//                        try {
//                            // Build getter method from cucumber table column name
//                            // TODO replace with         Method m = propertyDescriptor.getReadMethod();
//                            // See below how is used Method m = propertyDescriptor.getWriteMethod();
//                            String getterName = "get" +
//                                    columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
//                            Method method = refl.getMethod(getterName, paramTypes);
//                            Object valueOfInstance = method.invoke(instances.get(i));
//                            //
//                            var cucumberValue = cucumberMapColumnNameAndValue.getValue();
//                            // Replace with empty string
//                            if (replaceWithEmptyString != null && replaceWithEmptyString.length() > 0 && cucumberValue != null && cucumberValue.toString().trim().equals(replaceWithEmptyString.trim()))
//                                cucumberValue = "";
//                            // Case of null expected or given
//                            if (cucumberValue == null) {
//                                assertThat(valueOfInstance).isNull();
//                            } else if (valueOfInstance == null) {
//                                assertThat(valueOfInstance).isEqualTo(cucumberValue.toString());
//                            } else {
//                                switch (matchingField.typeName) {
//                                    case "Long":
//                                        assertThat(valueOfInstance).isEqualTo(Long.parseLong(cucumberValue.toString()));
//                                        break;
//                                    case "Double":
//                                        assertThat(valueOfInstance).isEqualTo(Double.parseDouble(cucumberValue.toString()));
//                                        break;
//                                    case "String":
//                                        assertThat(valueOfInstance.toString()).isEqualTo(cucumberValue.toString());
//                                        break;
//                                    case "Boolean":
//                                        assertThat(valueOfInstance).isEqualTo(Boolean.parseBoolean(cucumberValue.toString()));
//                                        break;
//                                    case "BigDecimal":
//                                        assertThat(valueOfInstance).isEqualTo(new BigDecimal(cucumberValue.toString()));
//                                        break;
//                                    default:
//                                        assertThat(valueOfInstance.toString()).isEqualTo(cucumberValue.toString());
//                                }
//                            }
//                        } catch (NoSuchMethodException e) {
//                            throw new CucumberException("Getter for attribute \"" + columnName + "\" not found for class " + instances.get(0).getClass().getName() + ".");
//
//                        } catch (AssertionError e) {
//                            CucumberException n = new CucumberException("Cucumber table column \"" + columnName + "\", row " + (i + 1) + " :\r\n" + e.getMessage());
//                            throw n;
//                        }
//                    }
//                }
//            }
            i++;

        }
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<String, String> entry, Class<T> entityClass) throws InstantiationException, IllegalAccessException {
        return newEntityInstanceFromMap(entry, entityClass, "[blank]");
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<String, String> entry, Class<T> entityClass, String replaceWithEmptyString) throws InstantiationException, IllegalAccessException {
        T instance;
        try {
            instance = entityClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException e) {
            throw new CucumberException("Class " + entityClass + " is missing  null-arg constructor (no arguments).");
        }
        Class<? extends Object> refl = instance.getClass();
        var paramTypes = new Class[1];
        String primaryKeyName = new EntityFields(instance.getClass()).getPrimaryKey();

        entry.forEach((k, v) -> {
            try {
                var propertyDescriptor = new PropertyDescriptor(k, refl);
                if (propertyDescriptor.getName().equalsIgnoreCase(primaryKeyName)) {
                    throw new CucumberException("Identifier property \"" + primaryKeyName + "\" cannot be used in Cucumber table because the value is autogenerated");
                }
                Method m = propertyDescriptor.getWriteMethod();
                //				BeanUtils.getWriteMethodParameter()
                // We suppose getter have 1 and only 1 parameter
                // get the actual param type
                var paramType0 = m.getParameterTypes()[0];
                paramTypes[0] = paramType0;
                if (v == null) {
                    m.invoke(instance, v);
                } else if (String.class.equals(paramType0)) {
                    if (v.equals(replaceWithEmptyString)) {
                        m.invoke(instance, "");
                    } else {
                        m.invoke(instance, v);
                    }
                } else if (int.class.equals(paramType0) || Integer.class.equals(paramType0)) {
                    m.invoke(instance, parseInt(v));
                } else if (Long.class.equals(paramType0)) {
                    m.invoke(instance, Long.parseLong(v));
                } else if (Double.class.equals(paramType0)) {
                    m.invoke(instance, Double.parseDouble(v));
                } else if (BigDecimal.class.equals(paramType0)) {
                    m.invoke(instance, new BigDecimal(v));
                } else if (byte.class.equals(paramType0) || Boolean.class.equals(paramType0)) {
                    m.invoke(instance, Boolean.valueOf(v));
                } else {
                    try {
                        // Whatever is the real type of attribute, it could happend that a setter with
                        // param as string exists. Thus, setter would convert the string to actual attribute type
                        m.invoke(instance, v);
                    } catch (Exception e) {
                        throw new CucumberException("CucumberUtils.newEntityInstanceFromMap to refactor for class " + paramTypes[0].getName());
                    }
                }
            } catch (CucumberException e) {
                throw e;
            } catch (Exception ex) {
                throw new CucumberException("Cucumber table has the named column : \"" + k + "\" ,\r\n" +
                        "Setter for attribute \"" + UpperCaseFirstLetter(k) + "\" with param type " + paramTypes[0] + " throws " + ex.getClass().getName() + "\r\n" + ex.getMessage());
            }
        });
        return instance;
    }

    static class EntityFields {

        private static class FieldDef {

            private String name;
            private String typeName;

            public FieldDef(String name, String typeName) {
                this.name = name;
                // Catch substring after period (.) if any
                Pattern pattern = Pattern.compile("([^.]+)$");
                Matcher m = pattern.matcher(typeName);
                if (m.find()) this.typeName = m.group();
            }

            public String getName() {
                return name;
            }

            public String getTypeName() {
                return typeName;
            }
        }

        private List<FieldDef> fields = new ArrayList(Arrays.asList());
        private String primaryKey;

        public EntityFields(EntityFields entityFields) {
        }

        public List<FieldDef> getFieldsNames() {
            return fields;
        }

        public void setFieldsNames(List<FieldDef> fields) {
            this.fields = fields;
        }

        public EntityFields(Class<?> pojo) {
            if (pojo == null) {
                this.fields = new ArrayList<>();
                this.primaryKey = null;
            } else {

                for (Field f : pojo.getDeclaredFields()) {

                    Annotation[] as = f.getAnnotations();
                    for (Annotation a : as) {
                        // Some other annotationType could be Column.class, JSONSerialize, ManyToMany, oneToMany, ...
                        if (a.annotationType() == Id.class) this.primaryKey = f.getName();
                    }
                    this.addField(f.getName(), f.getType().getName());
                }

                if (pojo.getSuperclass() != Object.class) {
                    var toConcat = new EntityFields(pojo.getSuperclass());
                    this.addFields(toConcat.getFields());
                    if (this.primaryKey == null) this.primaryKey = toConcat.primaryKey;
                }
            }
        }

        public List<FieldDef> addField(String name, String typeName) {
            if (name != null) this.fields.add(new FieldDef(name, typeName));
            return this.fields;
        }

        public List<FieldDef> addFields(List<FieldDef> newFields) {
            this.fields.addAll(newFields);
            return this.fields;
        }

        public List<FieldDef> getFields() {
            return this.fields;
        }

        public String getPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(String primaryKey) {
            this.primaryKey = primaryKey;
        }

        public String fieldsList() {
            return fields.stream().map(field -> field.getName()).collect(Collectors.joining(", "));
        }

        public FieldDef matching(String columnName) {
            if (columnName.length() < 1) return null;
            final String columNameLowerFirstChar = columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
            return this.fields.stream().filter(field ->
                    field.getName().equalsIgnoreCase(columnName)
                            || field.getName().equalsIgnoreCase(columNameLowerFirstChar)
            ).findFirst().orElse(null);
        }

    }

    private static String UpperCaseFirstLetter(String s) {
        if (s.length() >= 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return "";
    }

    public static List<?> createCollectionOfTransientEntitiesFromNameAndCucumberTable(String entityName, DataTable table) throws ClassNotFoundException {
        Class<?> type = findClassPerNameCanBePlural(entityName);
        List<?> entities =
                table.asMaps(String.class, String.class).stream().map(entry -> {
                    try {
                        return newEntityInstanceFromMap(entry, type);
                    } catch (CucumberException e) {
                        throw e;
                    } catch (Exception e) {
                    }
                    return null;
                }).collect(Collectors.toList());
        return entities;
    }

    public static Class<?> findClassPerNameCanBePlural(String entityName) {
        // An entity class name is reputed to start with an uppercase letter
        final String s = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        List<String> potentialClasses = Arrays.asList(
                s, // Uppercase first letter
                s.substring(0, entityName.length() - 1) // Uppercase first letter and delete potential ending s
        );
        final String[] entityNameSingular = new String[1];
        potentialClasses.stream().forEach(name -> {
                    try {
                        Class.forName(packageModelsPath + "." + name);
                        entityNameSingular[0] = name;
                    } catch (Exception e) {
                    }
                }
        );
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
            // Check if cucumber table value is empty string
            if (replaceWithEmptyString != null && replaceWithEmptyString.length() > 0 && cucumberFieldValue != null && cucumberFieldValue.toString().trim().equals(replaceWithEmptyString.trim())) {
                cucumberFieldValue = "";
            }
            Object gettedValueOfInstance = null;
            // Check if cucumber field name exists on instance class
            Field equivalentFieldInInstance = null;
            try {
                equivalentFieldInInstance = refl.getDeclaredField(cucumberFieldName);
            } catch (NoSuchFieldException e) {
                // Have a second try by un-capitalizing first letter
                cucumberFieldName = cucumberFieldName.substring(0, 1).toLowerCase() + cucumberFieldName.substring(1);
                try {
                    equivalentFieldInInstance = refl.getDeclaredField(cucumberFieldName);
                } catch (NoSuchFieldException e2) {
                    throw new CucumberException("Field name \"" + cucumberFieldName + "\" does not exist for entity \"" + refl.getSimpleName() + "\".\r\n" +
                            "List of attributes is " + Arrays.stream(refl.getDeclaredFields()).map(Field::getName).collect(Collectors.joining(", ")));
                }
            }
            // Check if this property is annotated with @Id and @GeneratedValue in entity class
            if (Arrays.stream(equivalentFieldInInstance.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 ||
                    Arrays.stream(equivalentFieldInInstance.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
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

            ///////////////  utiliser la suite dans le cas du writter !! ****************
            // Does a getter method simply returns string ?
//            try {
//                getterReturningString = refl.getMethod(getter.getName(), String.class);
//                gettedValueOfInstance = getterReturningString.invoke(new Class[0]);
//            } catch (NoSuchMethodException e) {
//                //  No getter returning string
//            } catch (InvocationTargetException | IllegalAccessException e) {
//                throw new CucumberException("Trying to get value for \"" + cucumberFieldName + "\" failed with error :\r\n" + e.getMessage());
//            }
//            if (getterReturningString != null) {
//                assertThat(gettedValueOfInstance).isEqualTo(cucumberFieldValue);
//            } else {
//                gettedValueOfInstance=refl.getter.getName()
            ////////////////   ********************

            // Try to compare with different numeric and date format
            // because toString() comparison would fail such 50 not equals to 50.0000
            if(cucumberFieldValue== null){
                assertThat(gettedValueOfInstance).isNull();
            }else{
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
            }}
        }); //row.forEach


    }
}
