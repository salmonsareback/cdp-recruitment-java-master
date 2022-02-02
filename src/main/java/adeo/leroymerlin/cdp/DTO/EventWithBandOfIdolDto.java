package adeo.leroymerlin.cdp.DTO;

import adeo.leroymerlin.cdp.models.Band;

import java.util.List;
import java.util.Set;

public class EventWithBandOfIdolDto {
    private String title;

    private String imgUrl;

    private List<BandWithIdolDto> bands;

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

    public List<BandWithIdolDto> getBands() {
        return bands;
    }

    public void setBands(List<BandWithIdolDto> bands) {
        this.bands = bands;
    }
}
