package au.com.samcday.bincrawl.tv.tvrage;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "shows")
public class ShowList {
    @JacksonXmlProperty(localName = "show")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Show> show;

    public List<Show> getShows() {
        return show;
    }

    public void setShows(List<Show> show) {
        this.show = show;
    }
}
