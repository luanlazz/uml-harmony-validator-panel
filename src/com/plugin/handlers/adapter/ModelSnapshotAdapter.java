package com.plugin.handlers.adapter;

public class ModelSnapshotAdapter {
    public final byte[] bytes;
    public final String fileName;

    public ModelSnapshotAdapter(byte[] bytes, String fileName) {
        this.bytes = bytes;
        this.fileName = fileName;
    }
}
