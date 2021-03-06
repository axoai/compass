package org.zalando.compass.library;

import com.google.common.collect.ImmutableMap;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.jooq.impl.DSL.row;
import static org.jooq.impl.DSL.val;
import static org.jooq.impl.DSL.values;

public final class Tables {

    private Tables() {

    }

    public static <K, V> Table<Record2<K, V>> table(final Map<K, V> dimensions,
            final Class<K> keyType, final Class<V> valueType) {

        final List<Row2<K, V>> rows = new ArrayList<>(dimensions.size());

        dimensions.forEach((key, value) ->
                rows.add(row(val(key, keyType), val(value, valueType))));

        @SuppressWarnings({"unchecked", "rawtypes"})
        final Row2<K, V>[] array = rows.toArray(new Row2[rows.size()]);

        return values(array);
    }

    public static <T, K, V> ImmutableMap<K, V> leftOuterJoin(final List<? extends T> result,
            final Function<? super T, ? extends K> keyFunction,
            final Function<? super T, ? extends V> valueFunction) {

        if (result.size() == 1) {
            final T record = result.get(0);

            // empty left join
            if (keyFunction.apply(record) == null) {
                return ImmutableMap.of();
            }
        }

        return result.stream().collect(toImmutableMap(keyFunction, valueFunction));
    }

}
