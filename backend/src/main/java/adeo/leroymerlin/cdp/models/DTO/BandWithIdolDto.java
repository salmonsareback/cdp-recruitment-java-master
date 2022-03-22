package adeo.leroymerlin.cdp.models.DTO;

import java.util.List;

public class BandWithIdolDto {
    private String name;

    private List<IdolDto> members;

    public BandWithIdolDto(String name) {
        this.name = name;
    }

    public BandWithIdolDto(String name, List<IdolDto> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<IdolDto> getMembers() {
        return members;
    }

    public void setMembers(List<IdolDto> members) {
        this.members = members;
    }
}
