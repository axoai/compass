package org.zalando.compass.domain.logic.value;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.compass.domain.logic.ValueService;
import org.zalando.compass.domain.model.Value;
import org.zalando.compass.domain.model.ValueRevision;

import java.util.List;
import java.util.Map;

@Service
class DefaultValueService implements ValueService {

    private final ReplaceValue replace;
    private final ReadValue read;
    private final DeleteValue delete;

    @Autowired
    DefaultValueService(final ReplaceValue replace, final ReadValue read, final DeleteValue delete) {
        this.replace = replace;
        this.read = read;
        this.delete = delete;
    }

    @Transactional
    @Override
    public boolean replace(final String key, final Value value) {
        return replace.replace(key, value);
    }

    @Transactional
    @Override
    public boolean replace(final String key, final List<Value> values) {
        return replace.replace(key, values);
    }

    @Override
    public List<Value> readAll(final String key, final Map<String, JsonNode> filter) {
        return read.readAll(key, filter);
    }

    @Override
    public Value read(final String key, final Map<String, JsonNode> filter) {
        return read.read(key, filter);
    }

    @Override
    public List<ValueRevision> readRevisions(final String key, final Map<String, JsonNode> filter) {
        return read.readRevisions(key, filter);
    }

    @Override
    public ValueRevision readRevision(final String key, final Map<String, JsonNode> filter, final long revision) {
        return read.readRevision(key, filter, revision);
    }

    @Transactional
    @Override
    public void delete(final String key, final Map<String, JsonNode> filter) {
        delete.delete(key, filter);
    }

}
