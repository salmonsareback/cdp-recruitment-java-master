package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
public class EventBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer nbStars;

    private String comment;

    private TestEnum oneEnum;

    @ManyToOne
    @JsonSerialize(using = ManagerSerializer.class)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    public EventBase() {
    }

    public EventBase(String title) {
        this.title = title;
    }

    public EventBase(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public EventBase(Long id, String title, Integer nbStars, String comment) {
        this.id = id;
        this.title = title;
        this.nbStars = nbStars;
        this.comment = comment;
    }

    public EventBase(String title, Integer nbStars, String comment) {
        this.title = title;
        this.nbStars = nbStars;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public TestEnum getOneEnum() {
        return oneEnum;
    }

    public void setOneEnum(TestEnum oneEnum) {
        this.oneEnum = oneEnum;
    }

    public void setOneEnum(String oneEnum) {
        this.oneEnum = TestEnum.valueOf(oneEnum);
    }
    public Integer getNbStars() {
        return nbStars;
    }

    public void setNbStars(Integer nbStars) {
        this.nbStars = nbStars;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }
}
