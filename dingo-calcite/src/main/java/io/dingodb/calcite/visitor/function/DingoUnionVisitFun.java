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

import io.dingodb.calcite.rel.DingoUnion;
import io.dingodb.calcite.visitor.DingoJobVisitor;
import io.dingodb.common.Location;
import io.dingodb.exec.base.IdGenerator;
import io.dingodb.exec.base.Job;
import io.dingodb.exec.base.Output;
import org.apache.calcite.rel.RelNode;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static io.dingodb.calcite.rel.DingoRel.dingo;

public class DingoUnionVisitFun {
    @NonNull
    public static List<Output> visit(
        Job job,
        IdGenerator idGenerator,
        Location currentLocation,
        DingoJobVisitor visitor,
        @NonNull DingoUnion rel
    ) {
        Collection<Output> inputs = new LinkedList<>();
        for (RelNode node : rel.getInputs()) {
            inputs.addAll(dingo(node).accept(visitor));
        }
        return DingoCoalesce.coalesce(idGenerator, inputs);
    }
}
