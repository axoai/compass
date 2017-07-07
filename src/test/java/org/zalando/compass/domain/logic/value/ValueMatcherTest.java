package org.zalando.compass.domain.logic.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.zalando.compass.domain.logic.relation.Equality;
import org.zalando.compass.domain.logic.relation.GreaterThanOrEqual;
import org.zalando.compass.domain.logic.relation.LessThanOrEqual;
import org.zalando.compass.domain.logic.relation.PrefixMatch;
import org.zalando.compass.domain.logic.relation.RegularExpression;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.zalando.compass.library.Schema.stringSchema;

public class ValueMatcherTest {
    
    private final RichDimension after = new RichDimension("after", stringSchema(), new GreaterThanOrEqual(), "");
    private final RichDimension before = new RichDimension("before", stringSchema(), new LessThanOrEqual(), "");
    private final RichDimension country = new RichDimension("country", stringSchema(), new Equality(), "");
    private final RichDimension postalCode = new RichDimension("postalCode", stringSchema(), new Equality(), "");
    private final RichDimension locale = new RichDimension("locale", stringSchema(), new PrefixMatch(), "");
    private final RichDimension email = new RichDimension("email", stringSchema(), new RegularExpression(), "");

    private final List<Map<RichDimension, JsonNode>> values = ImmutableList.of(
            of(country, text("CH"), before, text("2014-01-01T00:00:00Z")),
            of(country, text("CH"), before, text("2015-01-01T00:00:00Z")),
            of(country, text("CH"), after, text("2018-01-01T00:00:00Z")),
            of(country, text("CH"), after, text("2017-01-01T00:00:00Z")),
            of(country, text("DE"), after, text("2018-01-01T00:00:00Z")),
            of(country, text("DE"), after, text("2017-01-01T00:00:00Z")),
            of(country, text("DE"), postalCode, text("27498")),
            of(country, text("CH")),
            of(country, text("DE")),
            of(after, text("2017-01-01T00:00:00Z")),
            of(locale, text("de-DE")),
            of(locale, text("en-DE")),
            of(locale, text("de")),
            of(email, text(".*@zalando\\.de")),
            of(email, text(".*@goldmansachs\\.com")),
            of()
    );

    private final ValueMatcher unit = new ValueMatcher();

    @Test
    public void shouldMatchEquality() throws IOException {
        assertThat(unit.match(values, of(country, text("DE"))), contains(
                of(country, text("DE")),
                of()));
    }

    @Test
    public void shouldMatchEqualityFallback() throws IOException {
        assertThat(unit.match(values, of(country, text("UK"))), contains(
                of()));
    }

    @Test
    public void shouldMatchLessThan() throws IOException {
        assertThat(unit.match(values, of(country, text("CH"), before, text("2013-12-20T11:47:19Z"))), contains(
                of(country, text("CH"), before, text("2014-01-01T00:00:00Z")),
                of(country, text("CH"), before, text("2015-01-01T00:00:00Z")),
                of(country, text("CH")),
                of()));
    }

    @Test
    public void shouldMatchLessThanEqual() throws IOException {
        assertThat(unit.match(values, of(country, text("CH"), before, text("2014-01-01T00:00:00Z"))), contains(
                of(country, text("CH"), before, text("2014-01-01T00:00:00Z")),
                of(country, text("CH"), before, text("2015-01-01T00:00:00Z")),
                of(country, text("CH")),
                of()
        ));
    }

    @Test
    public void shouldMatchGreaterThan() throws IOException {
        assertThat(unit.match(values, of(country, text("CH"), after, text("2019-12-20T11:47:19Z"))), contains(
                of(country, text("CH"), after, text("2018-01-01T00:00:00Z")),
                of(country, text("CH"), after, text("2017-01-01T00:00:00Z")),
                of(country, text("CH")),
                of(after, text("2017-01-01T00:00:00Z")),
                of()));
    }

    @Test
    public void shouldMatchGreaterThanEqual() throws IOException {
        assertThat(unit.match(values, of(country, text("CH"), after, text("2018-01-01T00:00:00Z"))), contains(
                of(country, text("CH"), after, text("2018-01-01T00:00:00Z")),
                of(country, text("CH"), after, text("2017-01-01T00:00:00Z")),
                of(country, text("CH")),
                of(after, text("2017-01-01T00:00:00Z")),
                of()));
    }

    @Test
    public void shouldMatchPrefix() throws IOException {
        assertThat(unit.match(values, of(locale, text("de-AT"))),contains(
                of(locale, text("de")),
                of()));
    }

    @Test
    public void shouldMatchMatches() throws IOException {
        assertThat(unit.match(values, of(email, text("user@zalando.de"))), contains(
                of(email, text(".*@zalando\\.de")),
                of()));
    }

    @Test
    public void shouldMatchWithoutFilter() throws IOException {
        assertThat(unit.match(values, of()), contains(
                of()));
    }

    @Test
    public void shouldMatchWithUnknownDimensions() throws IOException {
        final RichDimension foo = new RichDimension("foo", stringSchema(), new Equality(), "");
        assertThat(unit.match(values, of(foo, text("bar"))), contains(
                of()));
    }

    @Test
    public void shouldMatchWithoutMatchingDimensions() throws IOException {
        assertThat(unit.match(values, of(postalCode, text("12345"))), contains(
                of()));
    }

    @Test
    public void shouldMatchWithPartiallyUnknownDimensions() throws IOException {
        final RichDimension foo = new RichDimension("foo", stringSchema(), new Equality(), "");
        assertThat(unit.match(values, of(country, text("DE"),
                foo, text("bar"))), contains(
                of(country, text("DE")),
                of()));
    }

    private JsonNode text(final String text) {
        return new TextNode(text);
    }

}