package org.zalando.compass.domain.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.compass.domain.model.Value;
import org.zalando.compass.domain.model.ValueId;
import org.zalando.compass.domain.persistence.model.tables.records.ValueDimensionRecord;
import org.zalando.compass.domain.persistence.model.tables.records.ValueRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.mapWithIndex;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.trueCondition;
import static org.jooq.impl.DSL.val;
import static org.jooq.impl.DSL.values;
import static org.zalando.compass.domain.persistence.ValueCriteria.withoutCriteria;
import static org.zalando.compass.domain.persistence.model.Tables.VALUE;
import static org.zalando.compass.domain.persistence.model.Tables.VALUE_DIMENSION;

@Component
public class ValueRepository implements Repository<Value, ValueId, ValueCriteria> {

    private final DSLContext db;

    @Autowired
    public ValueRepository(final DSLContext db) {
        this.db = db;
    }

    @Override
    public boolean create(final Value value) {
        final Long id = db.insertInto(VALUE)
                .columns(VALUE.KEY_ID, VALUE.VALUE_)
                .values(value.getKey(), value.getValue())
                .returning(VALUE.ID)
                .fetchOne().getId();

        final List<Query> queries = value.getDimensions().entrySet().stream()
                .map(e -> db.insertInto(VALUE_DIMENSION)
                        .columns(VALUE_DIMENSION.VALUE_ID, VALUE_DIMENSION.DIMENSION_ID,
                                VALUE_DIMENSION.DIMENSION_VALUE)
                        .values(id, e.getKey(), e.getValue()))
                .collect(toList());

        db.batch(queries).execute();

        return true;
    }

    @Override
    public Optional<Value> find(final ValueId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Value> findAll() {
        return findAll(withoutCriteria());
    }

    // TODO should we have cri
    @Override
    public List<Value> findAll(final ValueCriteria criteria) {
        return db.select()
                .from(VALUE)
                .leftJoin(VALUE_DIMENSION)
                .on(VALUE.ID.eq(VALUE_DIMENSION.VALUE_ID))
                .where(toCondition(criteria))
                .fetchGroups(ValueRecord.class, ValueDimensionRecord.class)
                .entrySet().stream()
                .map(this::map)
                .collect(toList());
    }

    private Condition toCondition(final ValueCriteria criteria) {
        if (criteria.getKey() != null) {
            return VALUE.KEY_ID.eq(criteria.getKey());
        } else if (criteria.getKeyPattern() != null) {
            return VALUE.KEY_ID.likeIgnoreCase(criteria.getKeyPattern());
        } else if (criteria.getDimension() != null) {
            return DSL.exists(selectOne()
                    .from(VALUE_DIMENSION)
                    .where(VALUE_DIMENSION.VALUE_ID.eq(VALUE.ID))
                    .and(VALUE_DIMENSION.DIMENSION_ID.eq(criteria.getDimension())));
        } else {
            return trueCondition();
        }
    }

    private Value map(final Entry<ValueRecord, List<ValueDimensionRecord>> entry) {
        final ValueRecord row = entry.getKey();

        final String key = row.getKeyId();

        final List<ValueDimensionRecord> result = entry.getValue();
        final ImmutableMap<String, JsonNode> dimensions = toMap(result);

        final JsonNode value = row.getValue();

        return new Value(key, dimensions, value);
    }

    private ImmutableMap<String, JsonNode> toMap(final List<ValueDimensionRecord> result) {

        if (result.size() == 1) {
            final ValueDimensionRecord record = result.get(0);

            // empty left join
            if (record.getDimensionId() == null) {
                return ImmutableMap.of();
            }
        }

        return result.stream().collect(toImmutableMap(
                ValueDimensionRecord::getDimensionId,
                ValueDimensionRecord::getDimensionValue));
    }

    @Override
    public boolean update(final Value value) {
        final int updates = db.update(VALUE)
                .set(VALUE.VALUE_, value.getValue())
                .where(VALUE.KEY_ID.eq(value.getKey()))
                .and(exactMatch(value.getDimensions()))
                .execute();

        return updates > 0;
    }

    // TODO replace?
    public boolean update(final List<Value> values) {
        final List<Query> queries = mapWithIndex(values.stream(), (value, index) ->
                db.update(VALUE)
                        .set(VALUE.VALUE_, value.getValue())
                        .set(VALUE.INDEX, index)
                        .where(VALUE.KEY_ID.eq(value.getKey()))
                        .and(exactMatch(value.getDimensions())))
                .collect(toList());

        return IntStream.of(db.batch(queries).execute()).sum() > 0;
    }

    @Override
    public void delete(final ValueId id) {
        final int deletions = db.deleteFrom(VALUE)
                .where(VALUE.KEY_ID.eq(id.getKey()))
                .and(exactMatch(id.getDimensions()))
                .execute();

        if (deletions > 1) {
            throw new AssertionError("Expected at most 1 value to be deleted, but matched " + deletions);
        }

        if (deletions == 0) {
            throw new NotFoundException();
        }
    }

    private Condition exactMatch(final Map<String, JsonNode> dimensions) {
        if (dimensions.isEmpty()) {
            return notExists(selectOne()
                    .from(VALUE_DIMENSION)
                    .where(VALUE_DIMENSION.VALUE_ID.eq(VALUE.ID)));
        } else {

            return notExists(selectOne()
                    .from(asTable(dimensions).as("expected", "dimension_id", "dimension_value"))
                    .fullOuterJoin(select(VALUE_DIMENSION.DIMENSION_ID, VALUE_DIMENSION.DIMENSION_VALUE)
                            .from(VALUE_DIMENSION)
                            .where(VALUE_DIMENSION.VALUE_ID.eq(VALUE.ID))
                            .asTable("actual"))
                    .using(field("dimension_id"), field("dimension_value"))
                    // TODO find out why coalesce doesn't work here
                    .where(field("actual.dimension_id").isNull())
                    .or(field("expected.dimension_id").isNull()));
        }
    }

    private Table<Record2<String, JsonNode>> asTable(final Map<String, JsonNode> dimensions) {
        final List<Row2<String, JsonNode>> rows = new ArrayList<>(dimensions.size());

        dimensions.forEach((id, value) ->
                rows.add(row(val(id, String.class), val(value, JsonNode.class))));

        @SuppressWarnings({"unchecked", "rawtypes"})
        final Row2<String, JsonNode>[] array = rows.toArray(new Row2[rows.size()]);

        return values(array);
    }

}