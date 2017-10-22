package io.whz.synapse.neural;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import io.whz.synapse.matrix.Matrix;
import io.whz.synapse.util.Precondition;

class ActivateFunction {
    @CheckResult
    static Matrix sigmoid(@NonNull Matrix matrix) {
        Precondition.checkNotNull(matrix);

        final Matrix copy = matrix.copy();
        final double[] doubles = copy.getArray();

        for (int i = 0, len = doubles.length; i < len; ++i) {
                final double cur = -doubles[i];
                doubles[i] = 1D / (1D + Math.exp(cur));
        }

        return copy;
    }

    @CheckResult
    static Matrix sigmoidPrime(@NonNull Matrix activation) {
        Precondition.checkNotNull(activation);

        final Matrix copy = activation.copy();
        final double[] doubles = copy.getArray();

        for (int i = 0, iLen = doubles.length; i < iLen; ++i) {
            final double cur = doubles[i];
            doubles[i] = cur * (1 - cur);
        }

        return copy;
    }
}
