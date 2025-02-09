/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.client.operation;

import io.dingodb.client.common.TableInfo;
import io.dingodb.client.operation.impl.*;
import io.dingodb.common.CommonId;
import io.dingodb.common.partition.RangeDistribution;
import io.dingodb.common.table.ColumnDefinition;
import io.dingodb.sdk.common.DingoCommonId;
import io.dingodb.sdk.common.SDKCommonId;
import io.dingodb.sdk.common.codec.KeyValueCodec;
import io.dingodb.sdk.common.table.Column;
import io.dingodb.sdk.common.table.Table;
import io.dingodb.sdk.common.utils.ByteArrayUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static io.dingodb.client.utils.OperationUtils.mapKeyPrefix;
import static io.dingodb.sdk.common.utils.Any.wrap;
import static io.dingodb.sdk.common.utils.ByteArrayUtils.lessThanOrEqual;

public class RangeUtils {

    public static boolean validateKeyRange(OpKeyRange keyRange) {
        return (!keyRange.getStart().userKey.isEmpty() || keyRange.withStart)
            && (!keyRange.getEnd().userKey.isEmpty() || keyRange.withEnd);
    }

    public static boolean validateOpRange(OpRange range) {
        return lessThanOrEqual(range.getStartKey(), range.getEndKey());
    }

    public static OpRange convert(KeyValueCodec codec, Table table, OpKeyRange keyRange) throws IOException {
        Object[] startKey = mapKeyPrefix(table, keyRange.start);
        Object[] endKey = mapKeyPrefix(table, keyRange.end);
        return new OpRange(
            codec.encodeKeyPrefix(startKey, keyRange.start.userKey.size()),
            codec.encodeKeyPrefix(endKey, keyRange.end.userKey.size()),
            keyRange.withStart,
            keyRange.withEnd
        );
    }

    public static Comparator<Operation.Task> getComparator() {
        return (e1, e2) -> ByteArrayUtils.compare(e1.<OpRange>parameters().getStartKey(), e2.<OpRange>parameters().getStartKey());
    }

    public static NavigableSet<Operation.Task> getSubTasks(TableInfo tableInfo, OpRange range) {
        return getSubTasks(tableInfo, range, null);
    }

    public static NavigableSet<Operation.Task> getSubTasks(TableInfo tableInfo, OpRange range, Coprocessor coprocessor) {
        Collection<RangeDistribution> src = tableInfo.rangeDistribution.values().stream()
            .map(RangeUtils::mapping)
            .collect(Collectors.toSet());
        RangeDistribution rangeDistribution = new RangeDistribution(
            mapping(tableInfo.tableId), range.getStartKey(), range.getEndKey(), range.withStart, range.withEnd);

        if (coprocessor == null) {
            return io.dingodb.common.util.RangeUtils.getSubRangeDistribution(src, rangeDistribution).stream()
                .map(rd -> new Operation.Task(
                    mapping(rd.id()),
                    wrap(new OpRange(rd.getStartKey(), rd.getEndKey(), rd.isWithStart(), rd.isWithEnd()))
                ))
                .collect(Collectors.toCollection(() -> new TreeSet<>(getComparator())));
        } else {
            return io.dingodb.common.util.RangeUtils.getSubRangeDistribution(src, rangeDistribution).stream()
                .map(rd -> new Operation.Task(
                    mapping(rd.id()),
                    wrap(new OpRangeCoprocessor(rd.getStartKey(), rd.getEndKey(), rd.isWithStart(), rd.isWithEnd(), coprocessor))
                ))
                .collect(Collectors.toCollection(() -> new TreeSet<>(getComparator())));
        }
    }

    public static CommonId mapping(DingoCommonId commonId) {
        return new CommonId(
            CommonId.CommonType.of(commonId.type().ordinal()),
            (int) commonId.parentId(),
            (int) commonId.entityId());
    }

    public static DingoCommonId mapping(CommonId commonId) {
        return new SDKCommonId(DingoCommonId.Type.values()[commonId.type.code], commonId.domain, commonId.seq);
    }

    public static RangeDistribution mapping(io.dingodb.sdk.common.table.RangeDistribution rangeDistribution) {
        return new RangeDistribution(
            mapping(rangeDistribution.getId()),
            rangeDistribution.getRange().getStartKey(),
            rangeDistribution.getRange().getEndKey()
        );
    }

    public static Coprocessor.AggregationOperator mapping(KeyRangeCoprocessor.Aggregation aggregation, Table table) {
        return new Coprocessor.AggregationOperator(aggregation.operation, table.getColumnIndex(aggregation.columnName));
    }

    public static ColumnDefinition mapping(Column column) {
        return ColumnDefinition.getInstance(
            column.getName(),
            column.getType().equals("STRING") ? "VARCHAR" : column.getType(),
            column.getElementType(),
            column.getPrecision(),
            column.getScale(),
            column.isNullable(),
            column.getPrimary(),
            column.getDefaultValue(),
            column.isAutoIncrement());
    }

    public static Column mapping(ColumnDefinition definition) {
        return io.dingodb.sdk.common.table.ColumnDefinition.builder()
            .name(definition.getName())
            .type(definition.getTypeName())
            .elementType(definition.getElementType())
            .precision(definition.getPrecision())
            .scale(definition.getScale())
            .nullable(definition.isNullable())
            .primary(definition.getPrimary())
            .defaultValue(definition.getDefaultValue())
            .build();
    }
}
