package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.regex.Regex;
import au.com.samcday.bincrawl.regex.RegexSource;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes a binary subject and attempts to classify it, using a database of regular expressions.
 */
@Singleton
public class BinaryClassifier {
    private static final Logger LOG = LoggerFactory.getLogger(BinaryClassifier.class);
    private static final Splitter PARTS_SPLITTER = Splitter.on(Pattern.compile("-|~| of |/")).trimResults();
    private static final Comparator<Regex> REGEX_COMPARATOR = new RegexComparator();

    private Timer classifyTimer = Metrics.newTimer(BinaryClassifier.class, "binaries", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    private SortedSet<Regex> regexList;

    @Inject
    public BinaryClassifier(RegexSource source) {
        this.regexList = new TreeSet<>(REGEX_COMPARATOR);
        this.regexList.addAll(source.load());
        LOG.info("Loaded {} regexes.", this.regexList.size());
    }

    public Classification classify(String group, String subject) {
        // TODO: TWR on timerCtx kgo.
        TimerContext timerCtx = this.classifyTimer.time();

        List<Regex> tests = this.findRegexForGroup(group);

        Matcher matching = null;
        for(Regex regex : tests) {
            Matcher m = regex.getPattern().matcher(subject);
            if(m.lookingAt()) {
                // Mind-boggling: only way to find out if a named capture group exists for current match is to catch
                // an IllegalArgumentException.
                try {
                    // We only wanna match this regex if it gives us both name and parts from subject.
                    if(!m.group("name").trim().isEmpty() && !m.group("parts").trim().isEmpty()) {
                        matching = m;
                        break;
                    }
                }
                catch(IllegalArgumentException iae) {
                    // Either "name" or "parts" named groups didn't exist, so it's a no go.
                }
            }
        }

        Classification classification = null;

        // TODO: handle reposts.
        // TODO: maybe handle reqids?

        if(matching != null) {
            classification = new Classification();
            classification.name = matching.group("name").trim();
            Iterator<String> partsParts /* <--- lolol */= PARTS_SPLITTER.split(matching.group("parts")).iterator();

            try {
                classification.partNum = Integer.parseInt(partsParts.next());
                classification.totalParts = Integer.parseInt(partsParts.next());
            }
            catch(NoSuchElementException|NumberFormatException e) {
                return null;
            }
        }

        timerCtx.stop();
        return classification;
    }

    private List<Regex> findRegexForGroup(String group) {
        // This method could probably be folded into a LoadingCache.
        List<Regex> matching = new ArrayList<>();
        for(Regex regex : this.regexList) {
            if(regex.matchesGroup(group)) {
                matching.add(regex);
            }
        }
        return matching;
    }

    public static class Classification {
        public String name;
        public int partNum;
        public int totalParts;
    }

    private static class RegexComparator implements Comparator<Regex> {
        @Override
        public int compare(Regex o1, Regex o2) {
            int v1 = o1.getOrdinal(), v2 = o2.getOrdinal();
            if(o1.getOrdinal() == o2.getOrdinal()) {
                v1 += o1.getId();
                v2 += o2.getId();
            }
            return v1 - v2;
        }
    }
}
