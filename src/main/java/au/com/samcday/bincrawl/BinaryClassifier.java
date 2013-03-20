package au.com.samcday.bincrawl;

import au.com.samcday.bincrawl.regex.Regex;
import au.com.samcday.bincrawl.regex.RegexSource;
import au.com.samcday.bincrawl.util.CloseableTimer;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static au.com.samcday.bincrawl.util.CloseableTimer.startTimer;

/**
 * Processes a binary subject and attempts to classify it, using a database of regular expressions.
 */
@Singleton
public class BinaryClassifier {
    private static final int CLASSIFICATION_CACHE_SIZE = 5000;
    private static final Logger LOG = LoggerFactory.getLogger(BinaryClassifier.class);
    private static final Splitter PARTS_SPLITTER = Splitter.on(Pattern.compile("-|~| of |/")).trimResults();
    private static final Comparator<Regex> REGEX_COMPARATOR = new RegexComparator();

    private Timer classifyTimer = Metrics.newTimer(BinaryClassifier.class, "classifications", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    private SortedSet<Regex> regexList;
    private LoadingCache<String, List<Regex>> groupRegexCache;
    private LoadingCache<GroupSubjectPair, Optional<Classification>> classificationCache;

    @Inject
    public BinaryClassifier(RegexSource source) {
        this.regexList = new TreeSet<>(REGEX_COMPARATOR);
        this.regexList.addAll(source.load());
        LOG.info("Loaded {} regexes.", this.regexList.size());

        this.groupRegexCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, List<Regex>>() {
                @Override
                public List<Regex> load(String group) throws Exception {
                    return getRegexListForGroup(group);
                }
            });

        this.classificationCache = CacheBuilder.newBuilder()
            .maximumSize(CLASSIFICATION_CACHE_SIZE)
            .build(new CacheLoader<GroupSubjectPair, Optional<Classification>>() {
                @Override
                public Optional<Classification> load(GroupSubjectPair key) throws Exception {
                    return Optional.fromNullable(doClassify(key.group, key.subject));
                }
            });
    }

    public Classification classify(String group, String subject) {
        return this.classificationCache.getUnchecked(GroupSubjectPair.of(group, subject)).orNull();
    }

    private Classification doClassify(String group, String subject) {
        try(CloseableTimer ignored = startTimer(this.classifyTimer)) {
            List<Regex> tests = this.groupRegexCache.getUnchecked(group);

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

            // TODO: handle reposts.
            // TODO: maybe handle reqids?

            Classification classification = null;
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

            return classification;
        }
    }

    private List<Regex> getRegexListForGroup(String group) {
        List<Regex> matching = new ArrayList<>();
        for(Regex regex : regexList) {
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

    private static class GroupSubjectPair {
        public static GroupSubjectPair of(String group, String subject) {
            GroupSubjectPair pair = new GroupSubjectPair();
            pair.group = group;
            pair.subject = subject;

            return pair;
        }

        String group;
        String subject;

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof  GroupSubjectPair)) return false;
            GroupSubjectPair other = (GroupSubjectPair)obj;
            return Objects.equals(this.group, other.group) && Objects.equals(this.subject, other.subject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.group, this.subject);
        }
    }
}
