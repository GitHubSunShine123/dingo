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

package io.dingodb.calcite.visitor.function;

import io.dingodb.calcite.rel.DingoGetByKeys;
import io.dingodb.calcite.utils.MetaServiceUtils;
import io.dingodb.calcite.utils.SqlExprUtils;
import io.dingodb.calcite.utils.TableInfo;
import io.dingodb.calcite.utils.TableUtils;
import io.dingodb.calcite.visitor.DingoJobVisitor;
import io.dingodb.codec.KeyValueCodec;
import io.dingodb.common.CommonId;
import io.dingodb.common.Location;
import io.dingodb.common.partition.RangeDistribution;
import io.dingodb.common.table.TableDefinition;
import io.dingodb.common.util.ByteArrayUtils;
import io.dingodb.exec.base.IdGenerator;
import io.dingodb.exec.base.Job;
import io.dingodb.exec.base.Output;
import io.dingodb.exec.base.Task;
import io.dingodb.exec.operator.EmptySourceOperator;
import io.dingodb.exec.operator.GetByKeysOperator;
import io.dingodb.exec.partition.PartitionStrategy;
import io.dingodb.exec.partition.RangeStrategy;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import static io.dingodb.common.util.NoBreakFunctions.wrap;

public final class DingoGetByKeysFun {
    private DingoGetByKeysFun() {
    }

    @NonNull
    public static List<Output> visit(
        Job job, IdGenerator idGenerator, Location currentLocation, DingoJobVisitor visitor, @NonNull DingoGetByKeys rel
    ) {
        final TableInfo tableInfo = MetaServiceUtils.getTableInfo(rel.getTable());
        final NavigableMap<ByteArrayUtils.ComparableByteArray, RangeDistribution> distributions = tableInfo.getRangeDistributions();
        final TableDefinition td = TableUtils.getTableDefinition(rel.getTable());
        final PartitionStrategy<CommonId, byte[]> ps = new RangeStrategy(td, distributions);
        final List<Output> outputs = new LinkedList<>();
        KeyValueCodec codec = TableUtils.getKeyValueCodecForTable(tableInfo.getId(), td);
        List<Object[]> keyTuples = TableUtils.getTuplesForKeyMapping(rel.getPoints(), td);
        if (keyTuples.isEmpty()) {
            EmptySourceOperator operator = new EmptySourceOperator();
            operator.setId(idGenerator.get());
            Task task = job.getOrCreate(currentLocation, idGenerator);
            task.putOperator(operator);
            outputs.addAll(operator.getOutputs());
            return outputs;
        }
        Map<CommonId, List<Object[]>> partMap = ps.partTuples(keyTuples, wrap(codec::encodeKey));
        for (Map.Entry<CommonId, List<Object[]>> entry : partMap.entrySet()) {
            GetByKeysOperator operator = new GetByKeysOperator(tableInfo.getId(), entry.getKey(), td.getDingoType(),
                td.getKeyMapping(), entry.getValue(), SqlExprUtils.toSqlExpr(rel.getFilter()), rel.getSelection()
            );
            operator.setId(idGenerator.get());
            Task task = job.getOrCreate(currentLocation, idGenerator);
            task.putOperator(operator);
            outputs.addAll(operator.getOutputs());
        }
        return outputs;
    }
}
