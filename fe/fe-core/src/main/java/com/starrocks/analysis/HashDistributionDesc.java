// This file is made available under Elastic License 2.0.
// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/analysis/HashDistributionDesc.java

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.analysis;

import com.google.common.collect.Lists;
import com.starrocks.catalog.AggregateType;
import com.starrocks.catalog.Column;
import com.starrocks.catalog.DistributionInfo;
import com.starrocks.catalog.DistributionInfo.DistributionInfoType;
import com.starrocks.catalog.HashDistributionInfo;
import com.starrocks.common.AnalysisException;
import com.starrocks.common.DdlException;
import com.starrocks.common.io.Text;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class HashDistributionDesc extends DistributionDesc {
    private int numBucket;
    private final List<String> distributionColumnNames;

    public HashDistributionDesc() {
        type = DistributionInfoType.HASH;
        distributionColumnNames = Lists.newArrayList();
    }

    public HashDistributionDesc(int numBucket, List<String> distributionColumnNames) {
        type = DistributionInfoType.HASH;
        this.numBucket = numBucket;
        this.distributionColumnNames = distributionColumnNames;
    }

    public List<String> getDistributionColumnNames() {
        return distributionColumnNames;
    }

    @Override
    public int getBuckets() {
        return numBucket;
    }

    @Override
    public void analyze(Set<String> cols) throws AnalysisException {
        if (numBucket <= 0) {
            throw new AnalysisException("Number of hash distribution is zero.");
        }
        if (distributionColumnNames == null || distributionColumnNames.size() == 0) {
            throw new AnalysisException("Number of hash column is zero.");
        }
        for (String columnName : distributionColumnNames) {
            if (!cols.contains(columnName)) {
                throw new AnalysisException("Distribution column(" + columnName + ") doesn't exist.");
            }
        }
    }

    @Override
    public String toSql() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DISTRIBUTED BY HASH(");
        int i = 0;
        for (String columnName : distributionColumnNames) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("`").append(columnName).append("`");
            i++;
        }
        stringBuilder.append(")\n");
        stringBuilder.append("BUCKETS ").append(numBucket);
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return toSql();
    }

    @Override
    public DistributionInfo toDistributionInfo(List<Column> columns) throws DdlException {
        List<Column> distributionColumns = Lists.newArrayList();

        // check and get distribution column
        for (String colName : distributionColumnNames) {
            boolean find = false;
            for (Column column : columns) {
                if (column.getName().equalsIgnoreCase(colName)) {
                    if (!column.isKey() && column.getAggregationType() != AggregateType.NONE) {
                        throw new DdlException("Distribution column[" + colName + "] is not key column");
                    }

                    if (!column.getType().canDistributedBy()) {
                        throw new DdlException(column.getType() + " column can not be distribution column");
                    }

                    distributionColumns.add(column);
                    find = true;
                    break;
                }
            }
            if (!find) {
                throw new DdlException("Distribution column[" + colName + "] does not found");
            }
        }

        return new HashDistributionInfo(numBucket, distributionColumns);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        super.write(out);

        out.writeInt(numBucket);
        int count = distributionColumnNames.size();
        out.writeInt(count);
        for (String colName : distributionColumnNames) {
            Text.writeString(out, colName);
        }
    }

    public void readFields(DataInput in) throws IOException {
        numBucket = in.readInt();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            distributionColumnNames.add(Text.readString(in));
        }
    }
}
