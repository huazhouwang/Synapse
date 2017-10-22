package io.whz.synapse.element;

import android.support.annotation.NonNull;

import java.io.File;

public class Dir {
    private static final String DOWNLOAD = "download";
    private static final String DECOMPRESS = "decompress";
    private static final String MNIST = "mnist";
    private static final String TRAIN = "train";
    private static final String TEST = "test";

    public final File root;
    public final File download;
    public final File decompress;
    public final File mnist;
    public final File train;
    public final File test;

    public Dir(@NonNull File root) {
        this.root = root;

        this.download = new File(root, DOWNLOAD);
        this.decompress = new File(root, DECOMPRESS);
        this.mnist = new File(root, MNIST);
        this.train = new File(this.mnist, TRAIN);
        this.test = new File(this.mnist, TEST);
    }
}
