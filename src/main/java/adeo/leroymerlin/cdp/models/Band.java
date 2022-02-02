package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name="Band.bandAndAllMembersWithAtLeastOneMemberHasNameLike", query =
"SELECT DISTINCT b FROM Band b JOIN b.members m  WHERE m.name LIKE :fewLetters")


@NamedQuery(name="Band.bandsWithEventsFromAListOfBandIdentifiers", query=
        "SELECT DISTINCT b FROM Band b JOIN FETCH b.events WHERE b.id IN :listOfBandsIdentifiers ")

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "name")
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(fetch=FetchType.EAGER)
    @JsonIgnore
    @JoinTable(name="band_members", joinColumns = {@JoinColumn(name="band_id")}, inverseJoinColumns = {@JoinColumn(name="members_id")})
    private Set<Member> members=new HashSet<>();

    @ManyToMany(mappedBy = "bands")
    private Set<Event> events=new HashSet<>();

    public Band() {}

    public Band(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Band(long l, String name, Event event) {
        this.id=l;
        this.name=name;
        this.setEvents(Set.of(event));
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

}
