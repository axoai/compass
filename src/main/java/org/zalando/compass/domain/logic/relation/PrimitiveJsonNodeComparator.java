package org.zalando.compass.domain.logic.relation;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Booleans;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

// TODO library?
final class PrimitiveJsonNodeComparator extends Ordering<JsonNode> {

    @Override
    public int compare(@Nullable final JsonNode left, @Nullable final JsonNode right) {
        if (left == null && right == null) {
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        }

        if (left.isNull() && right.isNull()) {
            return 0;
        } else if (left.isNull()) {
            return -1;
        } else if (right.isNull()) {
            return 1;
        }

        checkArgument(left.getNodeType() == right.getNodeType(),
                "JSON type mismatch: %s vs. %s", left.getNodeType(), right.getNodeType());

        switch (left.getNodeType()) {
            case ARRAY:
                return lexicographical().compare(left, right);
            case BOOLEAN:
                return Booleans.compare(left.booleanValue(), right.booleanValue());
            case NUMBER:
                return left.decimalValue().compareTo(right.decimalValue());
            case OBJECT:
                final List<String> leftNames = ImmutableList.sortedCopyOf(left::fieldNames);
                final List<String> rightNames = ImmutableList.sortedCopyOf(right::fieldNames);

                return leftNames.stream()
                        .reduce(ComparisonChain.start()
                                .compare(leftNames, rightNames, Ordering.natural().lexicographical()),
                                (chain, field) -> chain.compare(left.get(field), right.get(field), this),
                                (a, b) -> {
                                    throw new UnsupportedOperationException();
                                })
                        .result();
            case BINARY:
            case STRING:
                return left.asText().compareTo(right.asText());
        }

        // TODO better message
        throw new UnsupportedOperationException();
    }

}