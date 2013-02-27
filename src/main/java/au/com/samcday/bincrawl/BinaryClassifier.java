package au.com.samcday.bincrawl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.inject.Singleton;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Processes a binary subject and attempts to classify it, using a database of regular expressions.
 */
@Singleton
public class BinaryClassifier {
    private static final Logger LOG = LoggerFactory.getLogger(BinaryClassifier.class);
    private static final Splitter PARTS_SPLITTER = Splitter.on(Pattern.compile("-|~| of |/")).trimResults();

    private Timer classifyTimer = Metrics.newTimer(BinaryClassifier.class, "binaries", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    private ObjectMapper mapper = new ObjectMapper();
    private SortedSet<Regex> regexList;

    public BinaryClassifier() throws Exception {
        this.regexList = new TreeSet<Regex>(new Comparator<Regex>() {
            @Override
            public int compare(Regex o1, Regex o2) {
                int v1 = o1.ordinal, v2 = o2.ordinal;
                if(o1.ordinal == o2.ordinal) {
                    v1 += o1.pattern.hashCode();
                    v2 += o2.pattern.hashCode();
                }
                return v1 - v2;
            }
        });

        // The regex list will eventually come from somewhere sane.
        InputStream fin = Resources.getResource("regex.js").openStream();
        JsonNode root = mapper.readValue(fin, JsonNode.class);
        for(JsonNode node : ImmutableList.copyOf(root.elements())) {
            String patternStr = node.get("regex").textValue().trim();

            if(patternStr.isEmpty()) {
                continue; // Well an empty regex is kinda useless, isn't it?
            }

            int id = node.get("ID").asInt();
            int ordinal = node.get("ordinal").asInt(1);
            String groupName = node.get("groupname").textValue().trim();

            // This is the part where we ... java-friendlyify the regex pattern.

            // ?P<name> is a named capture group in PHP PCRE - but even they recommend to use the more standardized
            // syntax of ?<name>, which Java 7 supports.
            patternStr = patternStr.replace("?P<", "?<");

            // Cut off leading '/' regex start char.
            patternStr = patternStr.substring(1);

            // Now we parse out the trailing flags (if any) and convert them into Java flags bitfield.
            int patternFlags = 0;
            flagParse: while(true) {
                char flag = patternStr.charAt(patternStr.length()-1);
                patternStr = patternStr.substring(0, patternStr.length() - 1);

                switch(flag) {
                    case '/':
                        break flagParse;
                    case 'i':
                        patternFlags |= Pattern.CASE_INSENSITIVE;
                        break;
                    case 's':
                        patternFlags |= Pattern.DOTALL;
                        break;
                    case 'S':
                        // In php pcre land means to do further analysis to be more optimized in further calls.
                        break;
                    default:
                        throw new RuntimeException("Unhandled flag '" + flag + "' at end of regex ID " + id);
                }
            }

            try {
                Pattern pattern = Pattern.compile(patternStr, patternFlags);
                this.regexList.add(new Regex(pattern, groupName, ordinal, id));
            }
            catch(PatternSyntaxException pse) {
                LOG.warn("Regex ID {} is invalid", id, pse);
            }
        }

        LOG.info("Loaded {} regexes.", this.regexList.size());
    }

    public Classification classify(String group, String subject) {
        TimerContext timerCtx = this.classifyTimer.time();

        List<Regex> tests = this.findRegexForGroup(group);

        Matcher matching = null;
        for(Regex regex : tests) {
            Matcher m = regex.pattern.matcher(subject);
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
            // TODO: safety checks for part split? What if 0/1 entries?
            classification.partNum = Integer.parseInt(partsParts.next());
            classification.totalParts = Integer.parseInt(partsParts.next());
        }

        timerCtx.stop();
        return classification;
    }

    private List<Regex> findRegexForGroup(String group) {
        // This method could probably be folded into a LoadingCache.
        List<Regex> matching = new ArrayList<Regex>();
        for(Regex regex : this.regexList) {
            if(regex.matchesGroup(group)) {
                matching.add(regex);
            }
        }
        return matching;
    }

    private class Regex {
        int id;
        int ordinal;
        Pattern pattern;
        String group;
        private boolean groupPrefix = false;
        private boolean groupMatchAll = false;

        private Regex(Pattern pattern, String group, int ordinal, int id) {
            this.id = id;
            this.ordinal = ordinal;
            this.pattern = pattern;

            if(group.endsWith("*")) {
                group = group.substring(0, group.length() - 1);
                this.groupPrefix = true;
            }
            if(group.isEmpty()) {
                this.groupMatchAll = true;
            }
            this.group = group.toLowerCase();
        }

        boolean matchesGroup(String groupName) {
            if(this.groupPrefix) {
                if(this.groupMatchAll) return true;
                return groupName.toLowerCase().startsWith(this.group);
            }
            return this.group.equalsIgnoreCase(groupName);
        }
    }

    public static class Classification {
        String name;
        int partNum;
        int totalParts;
    }
}
