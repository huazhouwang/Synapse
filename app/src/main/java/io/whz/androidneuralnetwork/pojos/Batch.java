package io.whz.androidneuralnetwork.pojos;

import Jama.Matrix;

public class Batch {
    public final Matrix input;
    public final Matrix target;

    public Batch(double[][] input, double[][] target) {
        this.input = new Matrix(input);
        this.target = new Matrix(target);
    }
}
