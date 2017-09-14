package io.whz.androidneuralnetwork.pojos;

import io.whz.androidneuralnetwork.matrix.Matrix;

public class Batch {
    public final Matrix[] inputs;
    public final Matrix[] targets;

    public Batch(Matrix[] inputs, Matrix[] targets) {
        this.inputs = inputs;
        this.targets = targets;
    }
}
