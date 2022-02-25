package adeo.leroymerlin.cdp.models.DTO;

public class BandWithIdolAndIdDto extends BandWithIdolDto {
    private Long id;

    public BandWithIdolAndIdDto(Long id, String name){
        super(name);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
