package io.whz.synapse.pojo.neural;

import io.whz.synapse.matrix.Matrix;

public class Batch {
    public final Matrix[] inputs;
    public final Matrix[] targets;

    public Batch(Matrix[] inputs, Matrix[] targets) {
        this.inputs = inputs;
        this.targets = targets;
    }
}
