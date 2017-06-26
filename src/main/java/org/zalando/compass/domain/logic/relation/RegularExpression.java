package org.zalando.compass.domain.logic.relation;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.networknt.schema.JsonType;
import org.zalando.compass.domain.model.Relation;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.cache.CacheLoader.from;

public final class RegularExpression implements Relation {

    private final LoadingCache<String, Pattern> cache = CacheBuilder.newBuilder()
            // not 100% sure why we need that cast here
            .build(from((Function<String, Pattern>) Pattern::compile));

    @Override
    public String getId() {
        return "~";
    }

    @Override
    public String getTitle() {
        return "Regular expression";
    }

    @Override
    public String getDescription() {
        return "Matches values where the requested dimension values matches the configured regular expression.";
    }

    @Override
    public Set<JsonType> supports() {
        return Collections.singleton(JsonType.STRING);
    }

    @Override
    public boolean test(final JsonNode configured, final JsonNode requested) {
        return compile(configured.asText()).matcher(requested.asText()).matches();
    }

    private Pattern compile(final String pattern) {
        return cache.getUnchecked(pattern);
    }

    @Override
    public String toString() {
        return getId();
    }

}
