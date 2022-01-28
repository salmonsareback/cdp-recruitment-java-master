package adeo.leroymerlin.cdp.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

//    @OneToMany(fetch=FetchType.EAGER)
    @ManyToMany
    @JoinTable(name="band_members", joinColumns = {@JoinColumn(name="band_id")}, inverseJoinColumns = {@JoinColumn(name="members_id")})
    private Set<Member> members=new HashSet<Member>();

    @ManyToMany(mappedBy = "bands")
//    @JoinTable(name="event_bands", joinColumns = {@JoinColumn(name="bands_id")}, inverseJoinColumns = {@JoinColumn(name="event_id")})
    private Set<Event> events=new HashSet<>();

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
