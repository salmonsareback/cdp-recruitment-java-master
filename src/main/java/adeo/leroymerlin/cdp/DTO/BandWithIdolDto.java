package adeo.leroymerlin.cdp.DTO;

import adeo.leroymerlin.cdp.models.Member;

import java.util.Set;

public class BandWithIdolDto {
    private String name;

    private Set<Member> members;

    public BandWithIdolDto(String name) {
        this.name = name;
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
}
