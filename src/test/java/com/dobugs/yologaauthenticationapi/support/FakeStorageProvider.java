package com.dobugs.yologaauthenticationapi.support;

public class FakeStorageProvider implements StorageProvider {

    private final String BUCKET_NAME = "bucket";

    @Override
    public String bucket() {
        return BUCKET_NAME;
    }
}
