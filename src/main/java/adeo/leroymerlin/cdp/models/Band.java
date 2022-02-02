package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name="Band.bandAndAllMembersWithAtLeastOneMemberHasNameLike", query =
"SELECT DISTINCT b FROM Band b JOIN b.members m  WHERE m.name LIKE :fewLetters")


@NamedQuery(name="Band.bandsWithEventsFromAListOfBandIdentifiers", query=
        "SELECT DISTINCT b FROM Band b JOIN FETCH b.events WHERE b.id IN :listOfBandsIdentifiers ")

@Entity
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

//    @OneToMany(fetch=FetchType.EAGER)
    @ManyToMany(fetch=FetchType.EAGER)
    @JsonBackReference
    @JoinTable(name="band_members", joinColumns = {@JoinColumn(name="band_id")}, inverseJoinColumns = {@JoinColumn(name="members_id")})
    private Set<Member> members=new HashSet<>();

    @ManyToMany(mappedBy = "bands")
    @JsonManagedReference
    private Set<Event> events=new HashSet<>();

    public Band() {}

    public Band(Long id, String name) {
        this.id = id;
        this.name = name;
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
//    public Set<Event> getEvents() {
//        return events;
//    }
//
//    public void setEvents(Set<Event> events) {
//        this.events = events;
//    }
//
//    public void removeEvent(Event event){
//        this.events.remove(event);
//        event.getBands().remove(this);
//    }


}
