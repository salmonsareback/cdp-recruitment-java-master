package adeo.leroymerlin.cdp.models.DTO;

import adeo.leroymerlin.cdp.models.Band;

import java.io.Serializable;

public class BandWithFilteredMembersDto extends Band {
    private Integer nbOfMembers;

    public BandWithFilteredMembersDto(Long id, String name, Integer nbOfMembers) {
        super(id,name);
        this.nbOfMembers = nbOfMembers;
    }

    public Integer getNbOfMembers() {
        return nbOfMembers;
    }

    public void setNbOfMembers(Integer nbOfMembers) {
        this.nbOfMembers = nbOfMembers;
    }
}
