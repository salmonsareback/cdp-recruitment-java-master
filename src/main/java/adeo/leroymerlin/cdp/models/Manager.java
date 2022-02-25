package adeo.leroymerlin.cdp.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "firstName")
public class Manager {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long managerId;

    @Column(unique = true)
     private String firstName;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Event> events=new ArrayList<>();
}
