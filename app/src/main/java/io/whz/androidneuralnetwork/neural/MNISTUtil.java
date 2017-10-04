package io.whz.androidneuralnetwork.neural;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.whz.androidneuralnetwork.pojo.neural.Digit;
import io.whz.androidneuralnetwork.pojo.neural.Figure;

public class MNISTUtil {
    private static final int LABEL_MAGIC = 2049;
    private static final int IMAGE_MAGIC = 2051;
    private static final int BATCH_MAGIC = 2052;
    public static final int MAX_TRAINING_SIZE = 60000;
    public static final int PRE_FILE_SIZE = 2000;
    private static final String BATCH_FILE_SUFFIX = "batch";

    public static boolean gunzip(File sourceFile, File targetFile) {
        if (!sourceFile.exists()) {
            return false;
        }

        GZIPInputStream inputStream = null;
        FileOutputStream outputStream = null;

        boolean result;

        try {
            inputStream = new GZIPInputStream(new FileInputStream(sourceFile));
            outputStream = new FileOutputStream(targetFile);

            int len;
            final byte[] buffer = new byte[1024];

            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();

            result = false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return result;
    }

    public static List<Integer> parseLabels(File sourceFile) {
        DataInputStream inputStream = null;
        final List<Integer> list = new ArrayList<>();

        try {
            inputStream = new DataInputStream(new FileInputStream(sourceFile));

            final int magic = inputStream.readInt();

            if (magic != LABEL_MAGIC) {
                throw new IllegalArgumentException();
            }

            final int len = inputStream.readInt();
            byte cur;

            for (int i = 0; i < len; ++i) {
                cur = inputStream.readByte();
                list.add((int) cur);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return list;
    }

    public static Figure test(File file) {

        byte b = -1;
        Log.i("test2", (b & 0xff) + "");

        try {
            DataInputStream inputStream = new DataInputStream(new FileInputStream(file));

            final int magic = inputStream.readInt();
            final int len = inputStream.readInt();
            final int rows = inputStream.readInt();
            final int cols = inputStream.readInt();
            final int label = inputStream.readInt();

            final byte[] buffer = new byte[28 * 28];
            inputStream.read(buffer);

            return new Figure(label, buffer);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean parseImages(File sourceFile, File targetDir, List<Integer> labels) {
        if (!sourceFile.exists() || !targetDir.exists()) {
            return false;
        }

        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;

        boolean result;

        try {
            inputStream = new DataInputStream(new FileInputStream(sourceFile));

            final int magic = inputStream.readInt();

            if (magic != IMAGE_MAGIC) {
                throw new IllegalArgumentException();
            }

            final int len = inputStream.readInt();

            if (len != labels.size()) {
                throw new IllegalStateException();
            }

            final int rows = inputStream.readInt();
            final int cols = inputStream.readInt();
            final byte[] buffer = new byte[rows * cols];
            int count = 1;

            for (int i = 1; i <= len; ++i) {
                if (outputStream == null) {
                    outputStream = new DataOutputStream(new FileOutputStream(new File(targetDir,
                            String.format("%s.%s", count++, BATCH_FILE_SUFFIX))));
                    outputStream.writeInt(BATCH_MAGIC);
                    outputStream.writeInt(PRE_FILE_SIZE);
                    outputStream.writeInt(rows);
                    outputStream.writeInt(cols);
                }

                inputStream.read(buffer);
                outputStream.writeInt(labels.get(i - 1));
                outputStream.write(buffer);

                if (i % PRE_FILE_SIZE == 0) {
                    outputStream.flush();
                    outputStream.close();
                    outputStream = null;
                }
            }

            result = true;
        } catch (IOException e) {
            e.printStackTrace();

            result = false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static Digit[] readBatches(File file) {
        if (!file.exists()) {
            return null;
        }

        DataInputStream inputStream = null;

        try {
            inputStream = new DataInputStream(new FileInputStream(file));

            if (inputStream.readInt() != BATCH_MAGIC) {
                return null;
            }

            final int len = inputStream.readInt();
            final int rows = inputStream.readInt();
            final int cols = inputStream.readInt();

            final int size = rows * cols;
            final Digit[] digits = new Digit[len];
            final byte[] buffer = new byte[size];
            int curNum;

            for (int i = 0; i < len; ++i) {
                curNum = inputStream.readInt();
                inputStream.read(buffer);

                digits[i] = new Digit(curNum, convert(buffer));
            }

            return digits;
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static double[] convert(byte[] bytes) {
        final double[] doubles = new double[bytes.length];

        for (int i = 0, len = bytes.length; i < len; ++i) {
            doubles[i] = 0xFF & bytes[i];
        }

        return doubles;
    }

    public static String getLayerSizes(@NonNull int[] hiddenSizes) {
        final StringBuilder builder = new StringBuilder();
        final String spilt = " × ";

        builder.append(NeuralNetwork.INPUT_LAYER_NUMBER)
                .append(spilt);

        for (int size : hiddenSizes) {
            builder.append(size)
                    .append(spilt);
        }

        builder.append(NeuralNetwork.OUTPUT_LAYER_NUMBER);

        return builder.toString();
    }
}
