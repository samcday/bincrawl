package au.com.samcday.bincrawl.release.classification;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TvNameParser {
    private static final Pattern STD = Pattern.compile("^(.*?)[\\. \\-]s(\\d{1,2})\\.?e(\\d{1,3})(?:\\-e?|\\-?e)(\\d{1,3})\\.", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORMAT_S01E01 = Pattern.compile("^(.*?)[\\. \\-]s(\\d{1,2})\\.?e(\\d{1,3})\\.?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORMAT_1x01 = Pattern.compile("^(.*?)[\\. \\-](\\d{1,2})x(\\d{1,3})\\.", Pattern.CASE_INSENSITIVE);

    private static final CharMatcher SPACES = CharMatcher.anyOf("._-");
    private static final CharMatcher CHARS_A = CharMatcher.anyOf("àáâãäæÀÁÂÃÄ");
    private static final CharMatcher CHARS_C = CharMatcher.anyOf("çÇ");
    private static final CharMatcher CHARS_E = CharMatcher.anyOf("ΣèéêëÈÉÊË");
    private static final CharMatcher CHARS_I = CharMatcher.anyOf("ìíîïÌÍÎÏ");
    private static final CharMatcher CHARS_O = CharMatcher.anyOf("òóôõöÒÓÔÕÖ");
    private static final CharMatcher CHARS_U = CharMatcher.anyOf("ùúûüūÚÛÜŪ");

    public static TvInfo tryParse(String subject) {
        Matcher m;

        m = FORMAT_S01E01.matcher(subject);
        if(m.lookingAt()) {
            String name = clean(m.group(1));
            int season = Integer.parseInt(m.group(2));
            int episode = Integer.parseInt(m.group(3));
            return TvInfo.seasonAndEpisode(name, season, episode);
        }

        m = FORMAT_1x01.matcher(subject);
        if(m.lookingAt()) {
            String name = clean(m.group(1));
            int season = Integer.parseInt(m.group(2));
            int episode = Integer.parseInt(m.group(3));
            return TvInfo.seasonAndEpisode(name, season, episode);
        }

        return null;
    }

    public static final String clean(String name) {
        String cleaned = SPACES.trimAndCollapseFrom(name, ' ');
        cleaned = CHARS_A.replaceFrom(cleaned, "a");
        cleaned = CHARS_C.replaceFrom(cleaned, "c");
        cleaned = CHARS_E.replaceFrom(cleaned, "e");
        cleaned = CHARS_I.replaceFrom(cleaned, "i");
        cleaned = CHARS_O.replaceFrom(cleaned, "o");
        cleaned = CHARS_U.replaceFrom(cleaned, "u");
        return cleaned;
    }

    public static class TvInfo {
        private String name;
        private Type type;
        private int season;
        private List<Integer> episodes;

        public static final TvInfo seasonAndEpisode(String name, int season, int episode) {
            TvInfo info = new TvInfo();
            info.type = Type.SINGLE_EPISODE;
            info.name = name;
            info.season = season;
            info.episodes = ImmutableList.of(episode);
            return info;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getSeason() {
            return season;
        }

        public List<Integer> getEpisodes() {
            return episodes;
        }

        @Override
        public String toString() {
            return this.name +
                " season " + this.season +
                (type == Type.SINGLE_EPISODE ? " episode " + this.episodes.get(0) : (type == Type.MULTIPLE_EPISODES ?
                " episodes " + Joiner.on(",").join(this.episodes) : ""));
        }

        private static enum Type {
            SEASON,
            SINGLE_EPISODE,
            MULTIPLE_EPISODES
        }
    }
}
