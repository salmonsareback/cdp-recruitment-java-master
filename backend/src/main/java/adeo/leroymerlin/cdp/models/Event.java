package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "title")
public class Event extends EventBase {

    private String imgUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonSerialize(using = SetOfBandSerializer.class)
    @JoinTable(name = "event_bands", joinColumns = {@JoinColumn(name = "event_id", referencedColumnName = "id")}, inverseJoinColumns = {@JoinColumn(name = "bands_id", referencedColumnName = "id")})
    private Set<Band> bands = new HashSet<>();

    public Event(Long id, String title, String imgUrl, Set<Band> bands, Integer nbStars, String comment) {
        super(id, title, nbStars, comment);
        this.imgUrl = imgUrl;
        this.bands = bands;
    }

    public Event(Long id, String title, String imgUrl, Integer nbStars, String comment) {
        super(id, title, nbStars, comment);
        this.imgUrl = imgUrl;
    }

    public Event(String title, String imgUrl) {
        super(title);
        this.imgUrl = imgUrl;
    }

    public Event(String title, Integer nbStars, String comment) {
        super(title, nbStars, comment);
    }

    public Event(Long id, String title, String imgUrl) {
        super(id, title);
        this.imgUrl = imgUrl;
    }

    public Event(Long id, String title, Integer nbStars, String comment) {
        super(id, title, nbStars, comment);
    }

//
//    public String getImgUrl() {
//        return imgUrl;
//    }
//
//    public void setImgUrl(String imgUrl) {
//        this.imgUrl = imgUrl;
//    }
//
//
//    //    @JsonBackReference
//    public Set<Band> getBands() {
//        return bands;
//    }
//
//    public void setBands(Set<Band> bands) {
//        this.bands = bands;
//    }
//
    public void addBand(Band band) {
        this.bands.add(band);
    }

    public void removeBand(Band band) {
        this.bands.remove(band);
//        band.getEvents().remove(this);
    }

}
