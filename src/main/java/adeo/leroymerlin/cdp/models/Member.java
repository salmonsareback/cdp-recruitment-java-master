package adeo.leroymerlin.cdp.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    String name;

//    @ManyToMany(mappedBy = "members")
//    private Set<Band> bands=new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
