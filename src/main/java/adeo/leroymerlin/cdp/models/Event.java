package adeo.leroymerlin.cdp.models;

import adeo.leroymerlin.cdp.models.Band;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "title")
public class Event {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String title;

    private String imgUrl;

    @ManyToMany(fetch=FetchType.EAGER)
    @JsonSerialize(using = SetOfBandSerializer.class)
    @JoinTable(name="event_bands", joinColumns={@JoinColumn(name="event_id", referencedColumnName = "id")},inverseJoinColumns={@JoinColumn(name="bands_id", referencedColumnName = "id")})
    private Set<Band> bands = new HashSet<>();

    private Integer nbStars;

    private String comment;

    public Event(){}

    public Event(Long id, String title, String imgUrl, Set<Band> bands, Integer nbStars, String comment) {
        this.id = id;
        this.title = title;
        this.imgUrl = imgUrl;
        this.bands = bands;
        this.nbStars = nbStars;
        this.comment = comment;
    }

    public Event(Long id, String title, String imgUrl, Integer nbStars, String comment) {
        this.id = id;
        this.title = title;
        this.imgUrl = imgUrl;
        this.nbStars = nbStars;
        this.comment = comment;
    }

    public Event(String title, String imgUrl) {
        this.title = title;
        this.imgUrl = imgUrl;
    }

    public Event(String title, Integer nbStars, String comment) {
        this.title = title;
        this.nbStars = nbStars;
        this.comment = comment;
    }

    public Event(Long id, String title, String imgUrl) {
        this.id = id;
        this.title = title;
        this.imgUrl = imgUrl;
    }

    public Event(Long id, String title, Integer nbStars, String comment) {
        this.id = id;
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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

//    @JsonBackReference
    public Set<Band> getBands() {
        return bands;
    }

    public void setBands(Set<Band> bands) {
        this.bands = bands;
    }

    public void addBand(Band band){ this.bands.add(band);}

    public void removeBand(Band band){
        this.bands.remove(band);
//        band.getEvents().remove(this);
    }


    public Integer getNbStars() {
        return nbStars;
    }

    public void setNbStars(Integer nbStars) {
        this.nbStars = nbStars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
