// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Limited.

package com.starrocks.sql.optimizer.operator.physical;

import com.starrocks.analysis.JoinOperator;
import com.starrocks.sql.optimizer.OptExpression;
import com.starrocks.sql.optimizer.OptExpressionVisitor;
import com.starrocks.sql.optimizer.operator.OperatorType;
import com.starrocks.sql.optimizer.operator.OperatorVisitor;
import com.starrocks.sql.optimizer.operator.Projection;
import com.starrocks.sql.optimizer.operator.scalar.ScalarOperator;

import java.util.Objects;

public class PhysicalHashJoinOperator extends PhysicalJoinOperator {

    public PhysicalHashJoinOperator(JoinOperator joinType,
                                    ScalarOperator onPredicate,
                                    String joinHint,
                                    long limit,
                                    ScalarOperator predicate,
                                    Projection projection) {
        super(OperatorType.PHYSICAL_HASH_JOIN, joinType, onPredicate, joinHint, limit, predicate, projection);
    }

    @Override
    public <R, C> R accept(OperatorVisitor<R, C> visitor, C context) {
        return visitor.visitPhysicalHashJoin(this, context);
    }

    @Override
    public <R, C> R accept(OptExpressionVisitor<R, C> visitor, OptExpression optExpression, C context) {
        return visitor.visitPhysicalHashJoin(optExpression, context);
    }

    @Override
    public String toString() {
        return "PhysicalHashJoinOperator{" +
                "joinType=" + joinType +
                ", joinPredicate=" + onPredicate +
                ", limit=" + limit +
                ", predicate=" + predicate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PhysicalHashJoinOperator that = (PhysicalHashJoinOperator) o;
        return joinType == that.joinType && Objects.equals(onPredicate, that.onPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), joinType, onPredicate);
    }


}
