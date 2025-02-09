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

package io.dingodb.calcite.utils;

import io.dingodb.common.CommonId;
import io.dingodb.common.partition.Distribution;
import io.dingodb.common.partition.RangeDistribution;
import io.dingodb.common.util.ByteArrayUtils;
import lombok.RequiredArgsConstructor;

import java.util.NavigableMap;

@RequiredArgsConstructor
public class TableInfo {

    private final CommonId id;
    private final NavigableMap<ByteArrayUtils.ComparableByteArray, RangeDistribution> distributions;

    public CommonId getId() {
        return this.id;
    }

    public NavigableMap<ByteArrayUtils.ComparableByteArray, RangeDistribution> getRangeDistributions() {
        return this.distributions;
    }

}
