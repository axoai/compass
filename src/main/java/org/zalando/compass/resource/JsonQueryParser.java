package org.zalando.compass.resource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static com.google.common.base.CharMatcher.whitespace;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * A parser that takes a raw {@code Map<String, String>} usually passed as a query string, e.g.
 * {@code country=DE&age=32&active=true} and converts it to a {@code Map<String, JsonNode>}. It does so by guessing
 * the type based on the appearance of the value, e.g. if a value looks like a boolean it will become a
 * {@link com.fasterxml.jackson.databind.node.BooleanNode}.
 *
 * In order to prevent incorrect typing, e.g. the value {@code true} should stay a string, it needs to be wrapped in
 * double quotes: {@code "true"}.
 */
@Component
public class JsonQueryParser {

    private final ObjectMapper mapper;

    @Autowired
    public JsonQueryParser(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ImmutableMap<String, JsonNode> parse(final Map<String, String> filter) {
        return filter.isEmpty() ?
                ImmutableMap.of() :
                filter.entrySet().stream()
                    .collect(toImmutableMap(Map.Entry::getKey, e -> fromJson(e.getValue())));
    }

    private JsonNode fromJson(@Nullable final String value)  {
        if (value == null || whitespace().matchesAllOf(value)) {
            return NullNode.getInstance();
        } else {
            try {
                return mapper.readTree(value);
            } catch (final JsonParseException e) {
                return fromJson("\"" + value + "\"");
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}