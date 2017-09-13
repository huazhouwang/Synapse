package io.whz.androidneuralnetwork.matrix;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class MatrixTest {
    @Test
    public void testShape() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        assertArrayEquals(a.shape(), new int[]{
                3, 2
        });
    }

    @Test
    public void testRow() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        assertEquals(a.getRow(), 3);
    }

    @Test
    public void testCol() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        assertEquals(a.getCol(), 2);
    }

    @Test
    public void testArray() {
        final double[] original = new double[]{
                1D, 2D, 3D, 4D, 5D, 6D
        };
        final Matrix matrix = Matrix.array(original, 2);

        assertNotSame(original, matrix.getArray());
        assertEquals(matrix.getRow(), 2);
        assertEquals(matrix.getCol(), 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArray2() {
        final double[] original = new double[]{
                1D, 2D, 3D, 4D, 5D, 6D
        };

        Matrix.array(original, 4);
    }

    @Test
    public void testArray3() {
        final double[][] original = new double[][]{
                new double[]{1D, 2D, 3D},
                new double[]{4D, 5D, 6D}
        };

        final Matrix matrix = Matrix.array(original);

        assertEquals(matrix.getRow(), 2);
        assertEquals(matrix.getCol(), 3);
        assertArrayEquals(matrix.getArray(), new double[]{
                1D, 2D, 3D, 4D, 5D, 6D
        }, 0);
    }

    @Test
    public void testArrays() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 2);

        final double[][] arrays = a.getArrays();

        assertArrayEquals(arrays[0], new double[]{
                1, 2, 3
        }, 0);

        assertArrayEquals(arrays[1], new double[]{
                4, 5, 6
        }, 0);
    }

    @Test
    public void testCopy() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 2);

        final Matrix b = a.copy();

        assertNotSame(a, b);
        assertNotSame(a.getArray(), b.getArray());
        assertEquals(b.getRow(), 2);
        assertEquals(b.getCol(), 3);
        assertArrayEquals(b.getArray(), new double[]{
                1, 2, 3, 4, 5, 6
        }, 0);
    }

    @Test
    public void testZeros() {
        final Matrix matrix = Matrix.zeros(2, 3);

        assertEquals(matrix.getRow(), 2);
        assertEquals(matrix.getCol(), 3);
        assertArrayEquals(matrix.getArray(), new double[]{
                0D, 0D, 0D, 0D, 0D, 0D
        }, 0);
    }

    @Test
    public void testZerosLike() {
        final double[] aArr = new double[]{
                0D, 1D, 2D, 3D, 4D, 5D
        };

        final Matrix a = Matrix.array(aArr, 2);
        final Matrix b = Matrix.zeroLike(a);

        assertEquals(b.getRow(), 2);
        assertEquals(b.getCol(), 3);
        assertArrayEquals(b.getArray(), new double[]{
                0D, 0D, 0D, 0D, 0D, 0D
        }, 0);
    }

    @Test
    public void testRandn() {
        final Matrix a = Matrix.randn(2, 3);

        assertEquals(a.getRow(), 2);
        assertEquals(a.getCol(), 3);
    }

    @Test
    public void testTranspose() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 2);

        final Matrix b = a.transpose();

        assertEquals(b.getRow(), 3);
        assertEquals(b.getCol(), 2);
        assertArrayEquals(b.getArray(), new double[]{
                1, 4, 2, 5, 3, 6
        }, 0);
    }

    @Test
    public void testPlus() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix b = Matrix.array(new double[]{
                6, 5, 4, 3, 2, 1
        }, 3);

        final Matrix c = a.plus(b);

        assertNotSame(c, a);
        assertEquals(c.getRow(), 3);
        assertEquals(c.getCol(), 2);
        assertArrayEquals(c.getArray(), new double[]{
                7, 7, 7, 7, 7, 7
        }, 0);
    }

    @Test
    public void testPlusTo() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix b = Matrix.array(new double[]{
                6, 5, 4, 3, 2, 1
        }, 3);

        final Matrix c = a.plusTo(b);

        assertSame(c, a);
        assertArrayEquals(a.getArray(), new double[]{
                7, 7, 7, 7, 7, 7
        }, 0);
    }

    @Test
    public void testMinus() {
        final Matrix a = Matrix.array(new double[]{
                6, 6, 6, 6, 6, 6
        }, 2);

        final Matrix b = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 2);

        final Matrix c = a.minus(b);

        assertNotSame(a, c);
        assertEquals(c.getRow(), 2);
        assertEquals(c.getCol(), 3);
        assertArrayEquals(c.getArray(), new double[]{
                5, 4, 3, 2, 1, 0
        }, 0);
    }

    @Test
    public void testMinusTo() {
        final Matrix a = Matrix.array(new double[]{
                6, 6, 6, 6, 6, 6
        }, 2);

        final Matrix b = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 2);

        final Matrix c = a.minusTo(b);

        assertSame(c, a);
        assertArrayEquals(a.getArray(), new double[]{
                5, 4, 3, 2, 1, 0
        }, 0);
    }

    @Test
    public void testTimes() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix b = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix c = a.times(b);

        assertNotSame(c, a);
        assertEquals(c.getRow(), 3);
        assertEquals(c.getCol(), 2);
        assertArrayEquals(c.getArray(), new double[]{
                1, 4, 9, 16, 25, 36
        }, 0);
    }

    @Test
    public void testTimesTo() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix b = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix c = a.timesTo(b);

        assertSame(c, a);
        assertArrayEquals(a.getArray(), new double[]{
                1, 4, 9, 16, 25, 36
        }, 0);
    }

    @Test
    public void testTimes2() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix b = a.times(3);

        assertNotSame(b, a);
        assertEquals(b.getRow(), 3);
        assertEquals(b.getCol(), 2);
        assertArrayEquals(b.getArray(), new double[]{
                3, 6, 9, 12, 15, 18
        }, 0);
    }

    @Test
    public void testTimesTo2() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        final Matrix b = a.timesTo(3);

        assertSame(b, a);
        assertArrayEquals(a.getArray(), new double[]{
                3, 6, 9, 12, 15, 18
        }, 0);
    }

    @Test
    public void testGet() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        assertEquals(a.get(2,1), 6, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet2() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        a.get(1, 2);
    }

    @Test
    public void testSet() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        a.set(1, 1, 666);

        assertEquals(a.get(1, 1), 666, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet2() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 3);

        a.set(1, 2, 666);
    }

    @Test
    public void testDot() {
        final Matrix a = Matrix.array(new double[]{
                1, 2, 3, 4, 5, 6
        }, 2);

        final Matrix b = Matrix.array(new double[]{
                1, 2, 9, 4, 5, 7
        }, 3);

        final Matrix c = a.dot(b);

        assertEquals(c.getRow(), 2);
        assertEquals(c.getCol(), 2);
        assertArrayEquals(c.getArray(), new double[]{
                34, 31, 79, 70
        }, 0);
    }
}