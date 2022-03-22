package adeo.leroymerlin.cdp.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TestGeneric {
  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long thisIsTheId;

  private String title;

  public Long getThisIsTheId() {
    return thisIsTheId;
  }

  public void setThisIsTheId(Long thisIsTheId) {
    this.thisIsTheId = thisIsTheId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
