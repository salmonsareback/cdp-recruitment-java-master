package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name="Member.memberHavingPatternedNameAndItsBands", query =
        "SELECT DISTINCT m FROM Member m JOIN m.bands b  WHERE m.name LIKE :fewLetters")

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String name;

    @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<Band> bands=new HashSet<>();

    public Member() {}

    public Member(Long id, String name) {
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

    public Set<Band> getBands() {
        return bands;
    }

    public void setBands(Set<Band> bands) {
        this.bands = bands;
    }
}
