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

package io.dingodb.exec.utils;

import io.dingodb.common.Coprocessor;
import io.dingodb.common.table.ColumnDefinition;
import io.dingodb.common.type.DingoType;
import io.dingodb.common.type.NullableType;
import io.dingodb.common.type.TupleMapping;
import io.dingodb.common.type.TupleType;
import io.dingodb.expr.core.TypeCode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SchemaWrapperUtils {
    private SchemaWrapperUtils() {
    }

    private static ColumnDefinition mapDingoType(@NonNull NullableType type, int primary) {
        return ColumnDefinition.builder()
            .type(TypeCode.nameOf(type.getTypeCode()))
            .primary(primary)
            .nullable(type.isNullable())
            .build();
    }

    public static Coprocessor.SchemaWrapper buildSchemaWrapper(
        DingoType schema,
        @Nullable TupleMapping keyMapping,
        long id
    ) {
        TupleType tupleType = (TupleType) schema;
        int size = tupleType.fieldCount();
        if (keyMapping == null) {
            keyMapping = TupleMapping.of(Collections.emptyList());
        }
        TupleMapping reverseMapping = keyMapping.reverse(tupleType.fieldCount());
        List<ColumnDefinition> columnDefinitions = IntStream.range(0, size)
            .mapToObj(i -> {
                DingoType t = tupleType.getChild(i);
                return mapDingoType((NullableType) t, reverseMapping.get(i));
            })
            .collect(Collectors.toList());
        return Coprocessor.SchemaWrapper.builder()
            .schemas(columnDefinitions)
            .commonId(id)
            .build();
    }
}
