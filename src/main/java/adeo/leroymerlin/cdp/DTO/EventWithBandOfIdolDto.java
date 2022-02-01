package adeo.leroymerlin.cdp.DTO;

import adeo.leroymerlin.cdp.models.Band;

import java.util.Set;

public class EventWithBandOfIdolDto {
    private String title;

    private String imgUrl;

    private Set<Band> bands;

    public EventWithBandOfIdolDto(String title, String imgUrl) {
        this.title = title;
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Set<Band> getBands() {
        return bands;
    }

    public void setBands(Set<Band> bands) {
        this.bands = bands;
    }
}
