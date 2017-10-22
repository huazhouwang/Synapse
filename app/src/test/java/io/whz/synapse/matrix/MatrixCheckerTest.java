package io.whz.synapse.matrix;

import org.junit.Test;

public class MatrixCheckerTest {
    @Test
    public void testVerifyArrays() {
        final double[][] arrays = new double[][]{
                new double[]{1, 2, 3},
                new double[]{4, 5, 6}
        };

        MatrixChecker.verifyArrays(arrays);
    }

    @Test(expected = IllegalArgumentException.class)
     public void testVerifyArrays2() {
        MatrixChecker.verifyArrays(new double[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyArrays3() {
        final double[][] arrays = new double[][]{
                new double[]{1, 2, 3},
                new double[]{4, 5, 6, 4}
        };

        MatrixChecker.verifyArrays(arrays);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckDimensions() {
        MatrixChecker.checkDimensions(-1, 0);
    }

    @Test
    public void testCheckDimensions1() {
        MatrixChecker.checkDimensions( 2, 3);
    }

    @Test
    public void testCheckDimensions2() {
        final Matrix a = Matrix.zeros(2, 3);
        final Matrix b = a.copy();

        MatrixChecker.checkDimensions(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckDimensions3() {
        final Matrix a = Matrix.zeros(2, 3);
        final Matrix b = Matrix.zeros(3, 3);

        MatrixChecker.checkDimensions(a, b);
    }

    @Test
    public void testCheckInnerDimension() {
        final Matrix a = Matrix.zeros(2, 3);
        final Matrix b = Matrix.zeros(3, 2);

        MatrixChecker.checkInnerDimensions(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckInnerDimension2() {
        final Matrix a = Matrix.zeros(2, 3);
        final Matrix b = Matrix.zeros(3, 3);

        MatrixChecker.checkInnerDimensions(b, a);
    }
}