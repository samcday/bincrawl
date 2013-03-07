package au.com.samcday.bincrawl.tasks;

import au.com.samcday.bincrawl.BinaryClassifier;
import au.com.samcday.bincrawl.regex.Regex;
import au.com.samcday.bincrawl.regex.RegexSource;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.regex.Pattern;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class BinaryClassifierTests {
    @Test
    public void testBasicMatch() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(new Regex(Pattern.compile("^(?<name>\\s?test group\\s?) (?<parts>01/02)$"), "alt.test.group", 1, 1)));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", " test group  01/02");
        assertThat(res).isNotNull();
        assertThat(res.name).isEqualTo("test group");   // Also checking that whitespace is trimmed here.
        assertThat(res.totalParts).isEqualTo(2);
        assertThat(res.partNum).isEqualTo(1);
    }

    @Test
    public void testUnmatching() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(Collections.EMPTY_LIST);

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", "test group 01/02");
        assertThat(res).isNull();
    }

    @Test
    public void testMatchDifferentGroup() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(new Regex(Pattern.compile("^(?<name>test group) (?<parts>01/02)$"), "alt.test.group", 1, 1)));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group.lol.nope", "test group 01/02");
        assertThat(res).isNull();
    }

    @Test
    public void testMatchWithoutParts() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(new Regex(Pattern.compile("^(?<name>test group)"), "alt.test.group", 1, 1)));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", "test group");
        assertThat(res).isNull();
    }

    @Test
    public void testOrdinalPrecedence() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(
            new Regex(Pattern.compile("^(?<name>test group) (?<parts>01/02)$"), "alt.test.group", 1, 1),
            new Regex(Pattern.compile("^(?<name>test) group (?<parts>01/02)$"), "alt.test.group", 2, 2)
        ));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", "test group 01/02");
        assertThat(res).isNotNull();
        assertThat(res.name).isEqualTo("test group");
        assertThat(res.totalParts).isEqualTo(2);
        assertThat(res.partNum).isEqualTo(1);
    }

    @Test
    public void testIdPrecedence() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(
            new Regex(Pattern.compile("^(?<name>test group) (?<parts>01/02)$"), "alt.test.group", 1, 1),
            new Regex(Pattern.compile("^(?<name>test) group (?<parts>01/02)$"), "alt.test.group", 1, 2)
        ));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", "test group 01/02");
        assertThat(res).isNotNull();
        assertThat(res.name).isEqualTo("test group");
        assertThat(res.totalParts).isEqualTo(2);
        assertThat(res.partNum).isEqualTo(1);
    }

    @Test
    public void testMatchWithInvalidParts() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(new Regex(Pattern.compile("^(?<name>test group) (?<parts>.*)$"), "alt.test.group", 1, 1)));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", "test group 01");
        assertThat(res).isNull();

        res = classifier.classify("alt.test.group", "test group \u221E");
        assertThat(res).isNull();
    }

    @Test
    public void testOddPartDividors() {
        RegexSource mockRegexSource = Mockito.mock(RegexSource.class);
        when(mockRegexSource.load()).thenReturn(ImmutableList.of(new Regex(Pattern.compile("^(?<name>test group) (?<parts>.*)$"), "alt.test.group", 1, 1)));

        BinaryClassifier classifier = new BinaryClassifier(mockRegexSource);

        BinaryClassifier.Classification res = classifier.classify("alt.test.group", "test group 01~02");
        assertThat(res).isNotNull();

        res = classifier.classify("alt.test.group", "test group 01 ~ 02");
        assertThat(res).isNotNull();

        res = classifier.classify("alt.test.group", "test group 01 of 02");
        assertThat(res).isNotNull();

        res = classifier.classify("alt.test.group", "test group 01-02");
        assertThat(res).isNotNull();

        res = classifier.classify("alt.test.group", "test group 01 - 02");
        assertThat(res).isNotNull();
    }
}
