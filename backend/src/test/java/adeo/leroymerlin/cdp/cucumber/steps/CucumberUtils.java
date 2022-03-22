package adeo.leroymerlin.cdp.cucumber.steps;

import com.sun.istack.NotNull;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.datatable.DataTable;

import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
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
                throw new CucumberException("Cucumber table at row " + (i + 1) + " :\r\n" + e);
            }
            i++;
        }
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<String, Object> cucumberTableRow, Class<T> entityClass) {
        return newEntityInstanceFromMap(cucumberTableRow, entityClass, "[blank]");
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<Attribute<?, ?>, Object> mappedTableRow, EntityType<?> entityType) throws CucumberException {
        try {
            return newEntityInstanceFromMap(mappedTableRow, entityType, "[blank]");
        } catch (CucumberException ce) {
            throw ce;
        }
    }

    public static <T extends Object> T newEntityInstanceFromMap(Map<Attribute<?, ?>, Object> mappedTableRow, EntityType<?> entityType, String replaceWithEmptyString) throws CucumberException {
        T instance;
        try {
            instance = (T) entityType.getJavaType().getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CucumberException("Cucumber could not create a new instance of class " + entityType.getJavaType().getClass().getName() + ". One reason could be missing  null-arg constructor (no arguments).\r\n" + e);
        }

        Class<?> refl = instance.getClass();
        mappedTableRow.forEach((attribute, valueToSet) -> {
            // Get property descriptor (to get write method)
            PropertyDescriptor propertyDescriptor;
            try {
                propertyDescriptor = new PropertyDescriptor(attribute.getName(), refl);
            } catch (CucumberException e) {
                throw e;
            } catch (IntrospectionException e) {
                throw new CucumberException("Cucumber table has the named column : \"" + attribute.getName() + "\" ,\r\n" +
                        "Setter and/or getter for attribute \"" + attribute.getName() + "\" is missing.\r\n", e);
            }
            // Get Field (annotations)
            Field field;
            try {
                field = getRecursivelySuperClassDeclaredField(refl, attribute.getName());
                // Create write method
                Method m = propertyDescriptor.getWriteMethod();
                // TODO works only for implemented if @Enumerated(EnumType.STRING)
                boolean isEnum = field.getType().getSuperclass() == Enum.class;
                // Other method : isEnum = attribute.getJavaType().isEnum();
                // TODO works only for implemented if @Enumerated(EnumType.STRING)
                if (isEnum)
                    valueToSet = getValOfEnumType(field.getType(), valueToSet.toString());
                invokeSetterAdaptingValue(instance, m, valueToSet, replaceWithEmptyString, isEnum);
            } catch (CucumberException ce) {
                throw ce;
            } catch (NoSuchFieldException e) {
                throw new CucumberException("Field '" + attribute.getName() + "' of Cucumber table named column : '" + attribute.getName() + "' not found for " + attribute.getName() + ".", e);
            }

        }); // for each mapped table row
        return instance;
    }

    /**
     * Invoke method by parsing parameter value to expected type
     *
     * @param instance               the param type class reflex
     * @param setter                 the setter method
     * @param paramValue             the param value as object
     * @param replaceWithEmptyString the param value as string to replace by empty string
     * @param isEnum                 flag if parameter is an enum
     */
    private static <T extends Object> void invokeSetterAdaptingValue(T instance, Method setter, Object paramValue, String replaceWithEmptyString, boolean isEnum) {
        var paramTypes = new Class[1];
        try {
            // We suppose getter have 1 and only 1 parameter
            // get the actual param type
            var paramType = setter.getParameterTypes()[0];
            paramTypes[0] = paramType;
            if (paramValue == null) {
                setter.invoke(instance, paramValue);
            } else if (isEnum) {
                setter.invoke(instance, paramValue);
            } else if (String.class.equals(paramType)) {
                if (paramValue.equals(replaceWithEmptyString)) {
                    setter.invoke(instance, "");
                } else {
                    setter.invoke(instance, paramValue);
                }
            } else if (int.class.equals(paramType) || Integer.class.equals(paramType)) {
                setter.invoke(instance, parseInt(paramValue.toString()));
            } else if (Long.class.equals(paramType)) {
                setter.invoke(instance, Long.parseLong(paramValue.toString()));
            } else if (Double.class.equals(paramType)) {
                setter.invoke(instance, Double.parseDouble(paramValue.toString()));
            } else if (BigDecimal.class.equals(paramType)) {
                setter.invoke(instance, new BigDecimal(paramValue.toString()));
            } else if (byte.class.equals(paramType) || Boolean.class.equals(paramType)) {
                setter.invoke(instance, Boolean.valueOf(paramValue.toString()));
            } else {
                // Try to invoke with object (relation case)
                try {
                    setter.invoke(instance, paramValue);

                    // Whatever is the real type of attribute, it could happen that a setter with
                    // param as string exists. Thus, setter would convert the string to actual attribute type
                } catch (Exception e) {
                    paramTypes[0] = String.class;
                    var setWithParamString = instance.getClass().getMethod(setter.getName(), paramTypes);
                    setWithParamString.invoke(instance, paramValue);
                }
            }
        } catch (IllegalArgumentException | InvocationTargetException e) {
            throw new CucumberException("Failed when calling setter \"" + setter.getName() + "\" with String value \"" + paramValue + "\".", e);
        } catch (Exception e) {
            throw new CucumberException("Failed when calling setter \"" + setter.getName() + "\" with param type \"" + paramTypes[0].getName() + "\" for entity \"" + instance.getClass().getName() + "\".\r\n" +
                    "==> Could be solved by overloading setter " + setter.getName() + " casting string to adequate attribute type. Example :\r\n" +
                    "        public void setOneEnum(String oneEnum){\r\n" +
                    "         this.oneEnum = TestEnusetter.valueOf(oneEnum);\r\n" +
                    "        }\r\n", e);
        }
    }

    /**
     * Create a non persisted instance of entity according to a map attribute/value
     *
     * @param cucumberTableRow       : the mapping attribute/value
     * @param entityClass            : the entity class
     * @param replaceWithEmptyString : the value to be replaced by empty string
     * @param <T>
     * @return
     */
    public static <T extends Object> T newEntityInstanceFromMap(Map<String, Object> cucumberTableRow, Class<T> entityClass, String replaceWithEmptyString) {
        T instance;
        try {
            instance = entityClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CucumberException("Cucumber could not create a new instance of class " + entityClass + ". One reason could be missing  null-arg constructor (no arguments).\r\n" + e);
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
            // try a second chance by inverting first letter case
            if (fieldOfClass == null) {
                cucumberFieldName = invertCaseFirstLetter(cucumberFieldName);
                try {
                    fieldOfClass = getRecursivelySuperClassDeclaredField(refl, cucumberFieldName);
                } catch (NoSuchFieldException e2) {
                    fieldOfClass = null;
                }
            }
            if (fieldOfClass == null) {
                throw new CucumberException("Attribute \"" + cucumberFieldName + "\" does not exists for entity \"" + entityClass.getSimpleName() + "\".\r\n" + "List of attributes : " + getRecursivelySuperClassDeclaredFields(refl).stream().map(Field::getName).collect(Collectors.joining(", ")));
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
                    m.invoke(instance, parseInt(cucumberFieldValue.toString()));
                } else if (Long.class.equals(paramType0)) {
                    m.invoke(instance, Long.parseLong(cucumberFieldValue.toString()));
                } else if (Double.class.equals(paramType0)) {
                    m.invoke(instance, Double.parseDouble(cucumberFieldValue.toString()));
                } else if (BigDecimal.class.equals(paramType0)) {
                    m.invoke(instance, new BigDecimal(cucumberFieldValue.toString()));
                } else if (byte.class.equals(paramType0) || Boolean.class.equals(paramType0)) {
                    m.invoke(instance, Boolean.valueOf(cucumberFieldValue.toString()));
                } else if (fieldOfClass.getType().getSuperclass() == Enum.class) {
                    // TODO works only for implemented if @Enumerated(EnumType.STRING)
                    m.invoke(instance, getValOfEnumType(fieldOfClass.getType(), cucumberFieldValue.toString()));
                } else {
                    // Try to invoke with object (relation case)
                    try {
                        m.invoke(instance, cucumberFieldValue);
                    } catch (Exception ee) {

                        // Whatever is the real type of attribute, it could happend that a setter with
                        // param as string exists. Thus, setter would convert the string to actual attribute type
                        try {
                            paramTypes[0] = String.class;
                            var setWithParamString = refl.getMethod(m.getName(), paramTypes);
                            setWithParamString.invoke(instance, cucumberFieldValue);
                        } catch (IllegalArgumentException | InvocationTargetException e) {
                            throw new CucumberException("Cucumber could call setter for attribute \"" + cucumberFieldName + "\" with String value \"" + cucumberFieldValue + "\" but the error was returned :\r\n " + e.getCause() + "\r\n" + e);
                        } catch (Exception e) {
                            throw new CucumberException("Cucumber failed to use setter for attribute \"" + cucumberFieldName + "\" with param type \"" + paramTypes[0].getName() + "\" for entity \"" + refl.getSimpleName() + "\".\r\n" + "==> Could be solved by overloading setter set" + upperCaseFirstLetter(cucumberFieldName) + " casting string to adequate attribute type. Example :\r\n" + "        public void setOneEnum(String oneEnum){\r\n" + "          this.oneEnum = TestEnum.valueOf(oneEnum);\r\n" + "        }\r\n");
                        }
                    }
                }

            } catch (CucumberException e) {
                throw e;
            } catch (IntrospectionException e) {
                throw new CucumberException("Cucumber table has the named column : \"" + cucumberFieldName + "\" ,\r\n" + "Setter and/or getter for attribute \"" + cucumberFieldName + "\" is missing.\r\n");
            } catch (Exception ex) {
                throw new CucumberException("Cucumber table has the named column : \"" + cucumberFieldName + "\" ,\r\n" + "Setter for attribute \"" + cucumberFieldName + "\" with param type " + paramTypes[0] + " and value \"" + cucumberFieldValue.toString() + "\" throws " + ex.getClass().getName() + "\r\n" + ex.getMessage());
            }
        });
        return instance;
    }

    /**
     * Invoke setter for an instance
     *
     * @param instance     which a field should be set
     * @param fieldOfClass concerned
     * @param valueToSet   value to set (string)
     *                     No return : as an object the passed instance is modified
     */
    private static <T extends Object> void invokeSetterForInstance(T instance, Field fieldOfClass, String
            valueToSet) {
        var paramTypes = new Class[1];
        try {
            var propertyDescriptor = new PropertyDescriptor(fieldOfClass.getName(), instance.getClass());
            // Check if this property is annotated with @Id and @GeneratedValue in entity class
            if (Arrays.stream(fieldOfClass.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 && Arrays.stream(fieldOfClass.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
                throw new CucumberException("Identifier \"" + fieldOfClass.getName() + "\" is annotated @Id and @GeneratedValue. Values are unpredictable as being generated automatically. Such, cucumber table should not have header \"" + instance.getClass().getSimpleName() + "\".");
            }
            Method m = propertyDescriptor.getWriteMethod();
            // We suppose setter have 1 and only 1 parameter
            // get the actual param type
            var paramType0 = m.getParameterTypes()[0];
            paramTypes[0] = paramType0;
            if (valueToSet == null) {
                m.invoke(instance, valueToSet);
            } else if (String.class.equals(paramType0)) {
                m.invoke(instance, valueToSet);
            } else if (int.class.equals(paramType0) || Integer.class.equals(paramType0)) {
                m.invoke(instance, parseInt(valueToSet));
            } else if (Long.class.equals(paramType0)) {
                m.invoke(instance, Long.parseLong(valueToSet));
            } else if (Double.class.equals(paramType0)) {
                m.invoke(instance, Double.parseDouble(valueToSet));
            } else if (BigDecimal.class.equals(paramType0)) {
                m.invoke(instance, new BigDecimal(valueToSet));
            } else if (byte.class.equals(paramType0) || Boolean.class.equals(paramType0)) {
                m.invoke(instance, Boolean.valueOf(valueToSet));
                //TODO cast enum
            } else {
                try {
                    // Whatever is the real type of attribute, it could happend that a setter with
                    // param as string exists. Thus, setter would convert the string to actual attribute type
                    paramTypes[0] = String.class;
                    var setWithParamString = instance.getClass().getMethod(m.getName(), paramTypes);
                    setWithParamString.invoke(instance, valueToSet);
                } catch (IllegalArgumentException | InvocationTargetException e) {
                    throw new CucumberException("Cucumber could call setter for attribute \"" + fieldOfClass.getName() + "\" with String value \"" + valueToSet + "\" but the error was returned :\r\n " + e.getCause() + "\r\n" + e);
                } catch (Exception e) {
                    throw new CucumberException("Cucumber failed to use setter for attribute \"" + fieldOfClass.getName() + "\" with param type \"" + paramTypes[0].getName() + "\" for entity \"" + instance.getClass().getSimpleName() + "\".\r\n" + "==> Could be solved by overloading setter set" + upperCaseFirstLetter(fieldOfClass.getName()) + " casting string to adequate attribute type. Example :\r\n" + "        public void setOneEnum(String oneEnum){\r\n" + "          this.oneEnum = TestEnum.valueOf(oneEnum);\r\n" + "        }\r\n");
                }
            }
        } catch (CucumberException e) {
            throw e;
        } catch (IntrospectionException e) {
            throw new CucumberException("Cucumber table has the named column : \"" + fieldOfClass.getName() + "\" ,\r\n" + "Setter and/or getter for attribute \"" + fieldOfClass.getName() + "\" is missing.\r\n");
        } catch (Exception ex) {
            throw new CucumberException("Cucumber table has the named column : \"" + fieldOfClass.getName() + "\" ,\r\n" + "Setter for attribute \"" + fieldOfClass.getName() + "\" with param type " + paramTypes[0] + " throws " + ex.getClass().getName() + "\r\n" + ex.getMessage());
        }
    }

    /**
     * Find a field of an instance
     *
     * @param instance  of a class where the field should be returned
     * @param fieldName can be first letter uppercased or not
     * @return the field
     */
    private static <T extends Object> Field fieldOfInstance(T instance, String fieldName) {
        // This is a single field
        Field fieldOfClass;
        try {
            fieldOfClass = getRecursivelySuperClassDeclaredField(instance.getClass(), fieldName);
        } catch (NoSuchFieldException e) {
            fieldOfClass = null;
        }
        // try a second chance by inverting case of first letter
        if (fieldOfClass == null) {
            fieldName = invertCaseFirstLetter(fieldName);
            try {
                fieldOfClass = getRecursivelySuperClassDeclaredField(instance.getClass(), fieldName);
            } catch (NoSuchFieldException e2) {
                fieldOfClass = null;
            }
        }
        if (fieldOfClass == null) {
            throw new CucumberException("Attribute \"" + fieldName + "\" does not exists for entity \"" + instance.getClass().getSimpleName() + "\".\r\n" +
                    "List of attributes : "
                    + getRecursivelySuperClassDeclaredFields(instance.getClass()).stream().map(Field::getName).collect(Collectors.joining(", ")));
        }
        return fieldOfClass;
    }

    public static <T extends Object> Map<Field, Object> checkAndFormatMapForEntity(Map<String, Object> cucumberTableRow, Class<T> entityClass) {
        return checkAndFormatMapForEntity(cucumberTableRow, entityClass, "[blank]");
    }

    public static <T extends Object> Map<Attribute<?, ?>, Object> checkAndFormatMapForEntity(Map<Attribute<?, ?>, Object> mappedTableRow, EntityType<?> entityType) {
        return checkAndFormatMapForEntity(mappedTableRow, entityType, "[blank]");
    }

    public static <T extends Object> Map<Attribute<?, ?>, Object> checkAndFormatMapForEntity(Map<Attribute<?, ?>, Object> mappedTableRow, EntityType<?> entityType, String replaceWithEmptyString) {
        var returned = new HashMap<Attribute<?, ?>, Object>();
        mappedTableRow.forEach((attribute, paramValue) -> {
//            // Get property descriptor (to get write method)
//            PropertyDescriptor propertyDescriptor;
//            try {
//                propertyDescriptor = new PropertyDescriptor(attribute.getName(), refl);
//            } catch (CucumberException e) {
//                throw e;
//            } catch (IntrospectionException e) {
//                throw new CucumberException("Cucumber table has the named column : \"" + attribute.getName() + "\" ,\r\n" +
//                        "Setter and/or getter for attribute \"" + attribute.getName() + "\" is missing.\r\n", e);
//            }
            // Get Field (annotations)
            Field field;
            try {
                field = getRecursivelySuperClassDeclaredField(entityType.getJavaType(), attribute.getName());
//                // Create write method
//                Method m = propertyDescriptor.getWriteMethod();
//                // TODO works only for implemented if @Enumerated(EnumType.STRING)
//                boolean isEnum = field.getType().getSuperclass() == Enum.class;
//                // Other method : isEnum = attribute.getJavaType().isEnum();
//                // TODO works only for implemented if @Enumerated(EnumType.STRING)
//                if (isEnum)
//                    valueToSet = getValOfEnumType(field.getType(), valueToSet.toString());
//                invokeSetterAdaptingValue(instance, m, valueToSet, replaceWithEmptyString, isEnum);
                if (Arrays.stream(field.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 && Arrays.stream(field.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
                    throw new CucumberException("Identifier \"" + field.getName() + "\" is annotated @Id and @GeneratedValue. Values are unpredictable as being generated automatically. Such, cucumber table should not have header \"" + entityType.getName() + "\".");
                }
                // Check if replaceWithEmptyString
                if (paramValue == null) {
                    returned.put(attribute, "null");
                } else if (paramValue.toString().trim().equals(replaceWithEmptyString)) {
                    returned.put(attribute, "\"\"");
                } else if (field.getType().getSuperclass() == Enum.class) {
                    // TODO works only for implemented if @Enumerated(EnumType.STRING)
                    paramValue = getValOfEnumType(field.getType(), paramValue.toString());
                } else {
                    returned.put(attribute, paramValue);
                }

            } catch (NoSuchFieldException ef) {
                throw new CucumberException("Field '" + attribute.getName() + "' does not exists for entity \"" + entityType.getName() + "\".\r\n" +
                        "List of attributes : " + getRecursivelySuperClassDeclaredFields(entityType.getJavaType()).stream().map(Field::getName).collect(Collectors.joining(", ")), ef);
//                throw new CucumberException("Field '" + attribute.getName() + "' of Cucumber table named column : '" + attribute.getName() + "' not found for " + attribute.getName() + ".", e);
            } catch (CucumberException ce) {
                throw ce;
            }
        });
        return returned;
    }

    public static <T extends Object> Map<Field, Object> checkAndFormatMapForEntity(Map<String, Object> cucumberTableRow, Class<T> entityClass, String replaceWithEmptyString) {
//        T instance;
//        try {
//            instance = entityClass.getDeclaredConstructor().newInstance();
//        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
//            throw new CucumberException("Class " + entityClass + " is missing  null-arg constructor (no arguments).\r\n" + e);
//        }
//        Class<?> refl = instance.getClass();
//        var paramTypes = new Class[1];

        var returned = new HashMap<Field, Object>();

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
            // try a second chance by inverting first letter case
            if (fieldOfClass == null) {
                cucumberFieldName = invertCaseFirstLetter(cucumberFieldName);
                try {
                    fieldOfClass = getRecursivelySuperClassDeclaredField(entityClass, cucumberFieldName);
                } catch (NoSuchFieldException e2) {
                    fieldOfClass = null;
                }
            }
            if (fieldOfClass == null) {
                throw new CucumberException("Attribute \"" + cucumberFieldName + "\" does not exists for entity \"" + entityClass.getSimpleName() + "\".\r\n" + "List of attributes : " + getRecursivelySuperClassDeclaredFields(entityClass).stream().map(Field::getName).collect(Collectors.joining(", ")));
            }
//            try {
//                var propertyDescriptor = new PropertyDescriptor(cucumberFieldName, refl);
            // Check if this property is annotated with @Id and @GeneratedValue in entity class
            if (Arrays.stream(fieldOfClass.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0 && Arrays.stream(fieldOfClass.getAnnotations()).filter(a -> a.annotationType() == GeneratedValue.class).count() > 0) {
                throw new CucumberException("Identifier \"" + cucumberFieldName + "\" is annotated @Id and @GeneratedValue. Values are unpredictable as being generated automatically. Such, cucumber table should not have header \"" + entityClass.getSimpleName() + "\".");
            }
            // Check if replaceWithEmptyString
            if (cucumberFieldValue == null) {
                returned.put(fieldOfClass, "null");
            } else if (cucumberFieldValue.toString().trim().equals(replaceWithEmptyString)) {
                returned.put(fieldOfClass, "\"\"");
            } else {
                // Check if the value should be quoted or not
        /* §§§       if (fieldOfClass.getType() == String.class || fieldOfClass.getType() == char.class || fieldOfClass.getType() == Character.class) {
                    returned.put(fieldOfClass, "'" + cucumberFieldValue + "'");
                } else if (fieldOfClass.getType().getSuperclass() == Enum.class) {
                    // TODO works only for implemented if @Enumerated(EnumType.STRING)
                    returned.put(fieldOfClass, "'" + cucumberFieldValue + "'");
                    // Cope with Enums
//           TODO         returned.put(cucumberFieldName,getValOfEnumType(fieldOfClass.getType(),cucumberFieldValue).toString());
                } else {   §§§ */
                //TODO    fieldOfClass.isEnumConstant() ???
                returned.put(fieldOfClass, cucumberFieldValue);
                /* §§§    } §§§ */
            }
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

    public static String lowerCaseFirstLetter(String s) {
        if (s.length() >= 2) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        } else if (s.length() == 1) {
            return s.substring(0, 1).toLowerCase();
        }
        return "";
    }

    public static String invertCaseFirstLetter(String s) {
        if (!(s == null) && s.length() >= 1) {
            char firstChar = s.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return lowerCaseFirstLetter(s);
            } else {
                return upperCaseFirstLetter(s);
            }
        }
        return "";
    }

    public static List<?> createCollectionOfTransientEntitiesFromNameAndCucumberTable(String
                                                                                              entityName, DataTable table) {
        return createCollectionOfTransientEntitiesFromNameAndCucumberTable(entityName, table.asMaps(String.class, Object.class));
    }

    public static List<?> createListOfEntitiesFromListOfMapAttributesAndRelation(EntityType<?> entityType, List<Map<Attribute<?, ?>, Object>> listMapAttributes) {
        AtomicInteger rowNb = new AtomicInteger(0);
        List<?> entities = listMapAttributes.stream().map(entry -> {
            rowNb.getAndIncrement();
            try {
                return newEntityInstanceFromMap(entry, entityType);
            } catch (AssertionError | CucumberException e) {
                throw new CucumberException("Cucumber table at row " + rowNb + " :\r\n" + e);
            }
        }).collect(Collectors.toList());
        return entities;
    }

    public static List<?> createCollectionOfTransientEntitiesFromNameAndCucumberTable(String entityName, List<Map<String, Object>> tableAsListMap) {
        Class<?> type = findClassPerNameCanBePlural(entityName);
        AtomicInteger rowNb = new AtomicInteger(0);
        List<?> entities = tableAsListMap.stream().map(entry -> {
            rowNb.getAndIncrement();
            try {
                return newEntityInstanceFromMap(entry, type);
            } catch (AssertionError | CucumberException e) {
                throw new CucumberException("Cucumber table at row " + rowNb + " :\r\n" + e);
            }
        }).collect(Collectors.toList());
        return entities;
    }

    /**
     * Search metamodel entity name by trying different name spellings
     * first letter uppercase or lowercase
     * deleting last letter if s
     * replacing ending wording 'ies' by 'y'
     *
     * @param nameRead      (from Cucumber sentence)
     * @param entityManager , this class is not @PersistenceContext
     * @return entity type
     * @author LDuc
     */
    public static EntityType<?> findEntityTypePerNameThatCanHavePlural(String nameRead, EntityManager entityManager) {
        String name = nameRead.trim();
        List<String> namesToSearch = new ArrayList<>();
        namesToSearch.add(upperCaseFirstLetter(name));
        namesToSearch.add(lowerCaseFirstLetter(name));
        if (name.length() >= 2 && name.charAt(name.length() - 1) == 's') {
            namesToSearch.add(upperCaseFirstLetter(name.substring(0, name.length() - 1)));
            namesToSearch.add(lowerCaseFirstLetter(name.substring(0, name.length() - 1)));
        }
        if (name.length() >= 3 && name.substring(name.length() - 3).equals("ies")) {
            namesToSearch.add(upperCaseFirstLetter(name.substring(0, name.length() - 3) + "y"));
            namesToSearch.add(lowerCaseFirstLetter(name.substring(0, name.length() - 3) + "y"));
        }
        EntityType<?> entityType = null;
        Class clazz = null;
        for (String n : namesToSearch) {
            for (String packageName : packagesForModels()) {
                try {
                    clazz = Class.forName(packageName + "." + n);
                    for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
                        // Hibernate can insert entities with null Java types into the metamodel
                        if (entity.getJavaType() == null) continue;
                        if (entity.getJavaType().getName().equals(clazz.getName())) {
                            entityType = entity;
                            break;
                        }
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Just skip for next each
                }
            }// for package
            if (entityType != null) break;
        }// for names
        String nameTested = "[" + namesToSearch.stream().collect(Collectors.joining(", ")) + "]";
        if (clazz == null && entityType == null) {
            throw new CucumberException("No entity neither class exists for \"" + nameRead + "\", searched for " +
                    "[" + namesToSearch.stream().collect(Collectors.joining(", ")) + "].\r\n" +
                    "Class could not exist or not in a 'models' package. Packages scanned [" + packagesForModels().stream().collect(Collectors.joining(", ")) + "]"
            );
        }
        if (clazz != null && entityType == null) {
            throw new CucumberException("Class " + clazz.getName() + " found, but is not a persisted entity (in metamodel).\r\n" +
                    "Entities of metamodel [" + entityManager.getMetamodel().getEntities().stream().map(e -> e.getName()).collect(Collectors.joining(", ")) + "].\r\n");

        }
        return entityType;

    }

    // TODO replace findClassPerNameCanBePlural usages by findEntityTypePerNameThatCanHavePlural
    public static Class findClassPerNameCanBePlural(String entityName) {
// An entity class name is reputed to start with an uppercase letter
        final String s = upperCaseFirstLetter(entityName);
        List<String> potentialClasses = Arrays.asList(s, // Uppercase first letter
                s.substring(0, entityName.length() - 1) // Uppercase first letter and delete potential ending s
        );
        final String[] entityNameSingular = new String[1];
        String packageOfEntity = null;
        for (String name : potentialClasses) {
            for (String packageName : packagesForModels()) {
                try {
                    Class.forName(packageName + "." + name);
                    entityNameSingular[0] = name;
                    packageOfEntity = packageName;

                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    // Do nothing, let for boucle
                }
            }//for
        }

        if (entityNameSingular[0] == null)
            throw new CucumberException("No entity class exists for \"" + entityName + "\" (plural authorized) in any package.\r\n" +
                    "Entity could not exist or package is not declared properly.\r\n" +
                    "Packages examined :" + packagesForModels().stream().collect(Collectors.joining(", "))
            );
        //       "Entity could not exist or package is not declared properly, check or set cucumber.utils.models.package in application.properties.");
        try {
            Class<?> type = Class.forName(packageOfEntity + "." + entityNameSingular[0]);
            return type;
        } catch (Exception e) {
            throw new CucumberException("No entity class exists for \"" + entityName + "\" (plural authorized).");
        }
    }

    private static <T extends Object> void assertOneInstanceEqualsOneMapOfFieldsValues(T
                                                                                               instance, Map<String, String> row) throws AssertionError {
        assertOneInstanceEqualsOneMapOfFieldsValues(instance, row, "[blank]");
    }

    private static <T extends Object> void assertOneInstanceEqualsOneMapOfFieldsValues(T
                                                                                               instance, Map<String, String> row, String replaceWithEmptyString) throws AssertionError {
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
            // Have a second try by inverting first letter case
            if (equivalentFieldInInstance == null) {
                cucumberFieldName = invertCaseFirstLetter(cucumberFieldName);
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
                throw new CucumberException("Trying to get value for \"" + cucumberFieldName + "\" failed with error :\r\n" + e);
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
                            if (new BigDecimal(cucumberFieldValue).compareTo((BigDecimal) gettedValueOfInstance) != 0) // Let compare 2 bigdecimal with different leading zeros
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
                throw new CucumberException("Assertion error at column \"" + cucumberFieldName + "\"\n\r" + e);
            }
        }); //row.forEach
    }

    //     List of fields including those inherited
    public static List<Field> getRecursivelySuperClassDeclaredFields(Class clazz) {
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
    public static Field getRecursivelySuperClassDeclaredField(Class clazz, String fieldName) throws
            NoSuchFieldException {
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

    public static Field getRecursivelySuperClassFieldIdentifier(Class clazz) {
        return getRecursivelySuperClassDeclaredFields(clazz).stream().filter(field -> Arrays.stream(field.getAnnotations()).filter(a -> a.annotationType() == Id.class).count() > 0).findFirst().orElse(null);
    }

    private static <T extends Enum<T>> Object getValOfEnumType(Class T, String val) throws CucumberException {
        // TODO voir https://tomee.apache.org/examples-trunk/jpa-enumerated/
        var o2 = EnumSet.allOf(T).stream().filter(e -> e.toString().equals(val)).findFirst().orElse(null);
        if (o2 == null)
            throw new CucumberException("Value \"" + val + "\" does not belong to Enum " + T.getName() +
                    " [" + EnumSet.allOf(T).stream().map(e -> e.toString()).collect(Collectors.joining(", ")) + "].");
        return o2;

    }


    /**
     * Find potential packages containing models
     * pattern : ends with 'models'
     *
     * @return
     */
//  @Bean
// TODO Value could be listed in application.properties file
    private static List<String> packagesForModels() {
        List<Package> packages = Arrays.stream(Package.getPackages()).filter((p -> p.getName().endsWith("models"))).collect(Collectors.toList());
//        String msg = "\r\nCheck or set cucumber.utils.models.package in application.properties.";
        if (packages.size() == 0) {
            throw new CucumberException("No package ending with \"models\" was found in project" /*+ msg */);
//        } else if (packages.size() > 1) {
//            throw new CucumberException("Several packages containing \"models\" where found in project [" + packages.stream().map(p -> p.getName()).collect(Collectors.joining(", ")) + "]" + msg);
        }
        return packages.stream().map(p -> p.getName()).collect(Collectors.toList());
    }
}

