package au.com.samcday.bincrawl.regex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StaticRegexSource implements RegexSource {
    private static final Logger LOG = LoggerFactory.getLogger(StaticRegexSource.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Regex> load() {
        JsonNode root = null;

        // The regex list will eventually come from somewhere sane.
        try {
            InputStream fin = Resources.getResource("regex.js").openStream();
            root = mapper.readValue(fin, JsonNode.class);
        }
        catch(Exception ex) {
            Throwables.propagate(ex);
        }

        List<Regex> list = new ArrayList<>();
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
                list.add(new Regex(pattern, groupName, ordinal, id));
            }
            catch(PatternSyntaxException pse) {
                LOG.warn("Regex ID {} is invalid", id/*, pse*/);
            }
        }

        return list;
    }
}
