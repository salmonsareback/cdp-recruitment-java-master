package adeo.leroymerlin.cdp.cucumber.steps;

import io.cucumber.core.exception.CucumberException;
import org.junit.jupiter.api.Test;

import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.assertMappedCucumberTableEqualEntityInstances;
import static adeo.leroymerlin.cdp.cucumber.steps.CucumberUtils.newEntityInstanceFromMap;
import static org.assertj.core.api.Assertions.assertThat;

public class TestCucumberUtils {
  // TODO fetch models package
  String packageModelsPath = "adeo.leroymerlin.cdp.models";

  List<TestedClass> instances0 = Arrays.asList();

  List<TestedClass> instances1 = Arrays.asList(
    new TestedClass(1, 1253456L, "This is my first comment")
  );
  List<TestedClass> instances2 = Arrays.asList(
    new TestedClass(1, 1253456L, "This is my first comment"),
    new TestedClass(2, 98765432L, "This is my wrong comment")
  );
  List<TestedClass> instances3 = Arrays.asList(
    new TestedClass(1, 9223372036854775807L, "This is my first comment", 1223.1234567890124, new BigDecimal("12345678.123456789"), true),
    new TestedClass(2, 98765432L, "This is my second comment", 4.94065645841246544e-324d, new BigDecimal("12345.6789012345"), false)
  );
  List<Map<String, String>> rawTablewithId = Arrays.asList(
    Map.of("namedIdentifier", "1", "comment", "This is my first comment")
  );
  List<Map<String, String>> rawTablewithWrongAttribute = Arrays.asList(
    Map.of("comment", "This is my first comment", "commentary", "Superbe")
  );
  List<Map<String, String>> rawTableWith2Rows = Arrays.asList(
    Map.of("comment", "This is my first comment", "longDistance", "1253456"),
    Map.of("comment", "This is my wrong comment", "longDistance", "98765432")
  );
  List<Map<String, String>> rawTableWith2RowsAllTypes = Arrays.asList(
    Map.of("comment", "This is my first comment", "longDistance", "9223372036854775807", "doubleStars", "1223.1234567890124", "bigAmount", "12345678.123456789", "trueOrFalse", "true"),
    Map.of("comment", "This is my second comment", "longDistance", "98765432", "doubleStars", "4.94065645841246544e-324d", "bigAmount", "12345.6789012345", "trueOrFalse", "false")
  );
  List<Map<String, String>> rawTableWithWrongValue = Arrays.asList(
    Map.of("comment", "This is my first comment", "longDistance", "1253456"),
    Map.of("comment", "This is my second comment", "longDistance", "98765432")
  );

  @Test
  public void classOfEntityIsObject() {
    TestedClass testedClass = new TestedClass(1, 1253456L, "This is my first comment");
    assertThat(testedClass.getClass().getSuperclass().getName()).isEqualTo("java.lang.Object");
  }

  @Test
  public void usingIdentifierInCucumberTableThrowsCucumberException() throws Exception {
    var detected = false;
    try {
      assertMappedCucumberTableEqualEntityInstances(this.instances1, rawTablewithId);
    } catch (CucumberException e) {
      assertThat(e.getClass()).isEqualTo(CucumberException.class);
      assertThat(e.getMessage()).contains("Identifier \"namedIdentifier\" values should not be compared");
      detected = true;
    } finally {
      if (!detected) throw new Exception("CucumberException for identifier usage not detected");
    }
  }

  @Test
  public void usingWrongAttributeInCucumberTableThrowsCucumberException() throws Exception {
    var detected = false;
    try {
      assertMappedCucumberTableEqualEntityInstances(this.instances1, rawTablewithWrongAttribute);
    } catch (CucumberException e) {
      assertThat(e.getClass()).isEqualTo(CucumberException.class);
      assertThat(e.getMessage()).contains("Attribute \"commentary\" not found");
      assertThat(e.getMessage()).contains("Entity attributes are : namedIdentifier, longDistance, comment, doubleStars, bigAmount, trueOrFalse");
      detected = true;
    } finally {
      if (!detected)
        throw new Exception("CucumberException for wrong attribute not detected or fails return message.");
    }
  }

  @Test
  public void usingDifferentRowsInCucumberTableThrowsCucumberException() throws Exception {
    var detected = false;
    try {
      assertMappedCucumberTableEqualEntityInstances(this.instances1, rawTableWith2Rows);
    } catch (CucumberException e) {
      assertThat(e.getClass()).isEqualTo(CucumberException.class);
      assertThat(e.getMessage()).contains("number rows for both should be same.");
      detected = true;
    } finally {
      if (!detected) throw new Exception("CucumberException for not equivalent of rows not detected");
    }
  }

  @Test
  public void usingNoInstancesThrowsCucumberException() throws Exception {
    var detected = false;
    try {
      assertMappedCucumberTableEqualEntityInstances(this.instances0, rawTableWith2Rows);
    } catch (CucumberException e) {
      assertThat(e.getClass()).isEqualTo(CucumberException.class);
      assertThat(e.getMessage()).contains("Neither entities fetched or cucumber table should have no row.");
      detected = true;
    } finally {
      if (!detected) throw new Exception("CucumberException for not equivalent of rows not detected");
    }
  }

  @Test
  public void cucumberTableWithIntStringAndLongMatchingNotThrowsException() throws Exception {
    assertMappedCucumberTableEqualEntityInstances(this.instances2, rawTableWith2Rows);
    // Nothing to assert, no exception !
  }

  @Test
  public void cucumberTableWithDoubleBigDecimalMatchingNotThrowsException() throws Exception {
    assertMappedCucumberTableEqualEntityInstances(this.instances3, rawTableWith2RowsAllTypes);
    // Nothing to assert, no exception !
  }

  @Test
  public void usingDifferentDataInCucumberTableThrowsCucumberException() throws Exception {
    var detected = false;
    try {
      assertMappedCucumberTableEqualEntityInstances(this.instances2, rawTableWithWrongValue);
    } catch (CucumberException e) {
      assertThat(e.getClass()).isEqualTo(CucumberException.class);
      assertThat(e.getMessage()).contains("Cucumber table column \"comment\", row 2");
      detected = true;
    } finally {
      if (!detected)
        throw new Exception("CucumberException for wrong data not detected or fails return message.");
    }
  }

  @Test
  public void differentTypeOfDataCouldBeUsedInCucumberTable() throws InvocationTargetException, InstantiationException, NoSuchMethodException, IllegalAccessException {
    newEntityInstanceFromMap(rawTableWith2RowsAllTypes.get(0), TestedClass.class);
  }

  /*
  aliases
  @DatatableType a utiliser quand ??? ¨% methode setter
  [null], [blank]
   */
  public static class TestedClass implements Serializable {
    @Id
    private Integer namedIdentifier;
    private Long longDistance;
    private String comment;
    private Double doubleStars;
    private BigDecimal bigAmount;
    private Boolean trueOrFalse;

    public TestedClass() {
    }

    public TestedClass(Integer namedIdentifier, Long longDistance, String comment) {
      this.namedIdentifier = namedIdentifier;
      this.longDistance = longDistance;
      this.comment = comment;
    }

    public TestedClass(Integer namedIdentifier, Long longDistance, String comment, Double doubleStars, BigDecimal bigAmount, Boolean trueOrFalse) {
      this.namedIdentifier = namedIdentifier;
      this.longDistance = longDistance;
      this.comment = comment;
      this.doubleStars = doubleStars;
      this.bigAmount = bigAmount;
      this.trueOrFalse = trueOrFalse;
    }

    public Integer getNamedIdentifier() {
      return namedIdentifier;
    }

    public void setNamedIdentifier(Integer namedIdentifier) {
      this.namedIdentifier = namedIdentifier;
    }

    public Long getLongDistance() {
      return longDistance;
    }

    public void setLongDistance(Long longDistance) {
      this.longDistance = longDistance;
    }

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    public Double getDoubleStars() {
      return doubleStars;
    }

    public void setDoubleStars(Double doubleStars) {
      this.doubleStars = doubleStars;
    }

    public BigDecimal getBigAmount() {
      return bigAmount;
    }

    public void setBigAmount(BigDecimal bigAmount) {
      this.bigAmount = bigAmount;
    }

    public Boolean getTrueOrFalse() {
      return trueOrFalse;
    }

    public void setTrueOrFalse(Boolean trueOrFalse) {
      this.trueOrFalse = trueOrFalse;
    }
  }


}
