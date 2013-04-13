package au.com.samcday.bincrawl.tv;

import au.com.samcday.bincrawl.RedisKeys;
import au.com.samcday.bincrawl.pool.BetterJedisPool;
import au.com.samcday.bincrawl.pool.PooledJedis;
import au.com.samcday.bincrawl.tv.tvrage.Show;
import au.com.samcday.bincrawl.tv.tvrage.ShowList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class TvRage {
    private static final Pattern HAS_YEAR = Pattern.compile("^(.*?)\\s*(\\d{2,4})$");
    private static final String SHOW_LIST_URL = "http://services.tvrage.com/feeds/show_list.php";

    private Map<String, Integer> shows;
    private BetterJedisPool redisPool;
    private ObjectMapper objectMapper;
    private HttpClient httpClient;

    @Inject
    public TvRage(BetterJedisPool redisPool, ObjectMapper objectMapper, HttpClient httpClient) {
        this.redisPool = redisPool;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.shows = new HashMap<>();

        try {
            this.init();
        }
        catch(Exception e) {
            Throwables.propagate(e);
        }
    }

    private void init() throws IOException {
        try(PooledJedis redisClient = this.redisPool.get()) {
            String data = redisClient.get(RedisKeys.tvRage);
            if(data != null) {
                ShowList list = this.objectMapper.readValue(data, ShowList.class);
                this.buildShowsFromList(list);
            }
        }
    }

    public void refreshList() throws Exception {
        HttpGet get = new HttpGet(SHOW_LIST_URL);
        HttpResponse resp = this.httpClient.execute(get);
        // TODO: TWR support for httpclient mebbe?
        try {
            HttpEntity entity = resp.getEntity();
            XmlMapper xmlMapper = new XmlMapper();
            ShowList list = xmlMapper.readValue(entity.getContent(), ShowList.class);
            this.buildShowsFromList(list);
            EntityUtils.consume(entity);

            try(PooledJedis redisClient = this.redisPool.get()) {
                redisClient.set(RedisKeys.tvRage, this.objectMapper.writeValueAsString(list));
            }
        }
        finally {
            get.releaseConnection();
        }
    }

    private void buildShowsFromList(ShowList showList) {
        this.shows = new HashMap<>();
        for(Show show : showList.getShows()) {
            shows.put(show.getName().toLowerCase().replace("'", ""), show.getId());
        }
    }

    public Integer getShowId(String showName) {
        // If the show name ends with a number, then let's see if it's qualifying the year.
        Matcher hasYearMatcher = HAS_YEAR.matcher(showName);
        if(hasYearMatcher.matches()) {
            String qualifiedShowName = hasYearMatcher.group(1).toLowerCase() + " (" + hasYearMatcher.group(2) + ")";
            if(this.shows.containsKey(qualifiedShowName)) {
                return this.shows.get(qualifiedShowName);
            }
        }
        return this.shows.get(showName.toLowerCase());
    }
}
