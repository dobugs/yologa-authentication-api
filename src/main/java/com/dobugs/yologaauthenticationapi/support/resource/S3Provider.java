package com.dobugs.yologaauthenticationapi.support.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dobugs.yologaauthenticationapi.support.StorageProvider;

@Component
public class S3Provider implements StorageProvider {
    
    private final String bucket;

    public S3Provider(
        @Value("${spring.cloud.aws.s3.bucket}") final String bucket
    ) {
        this.bucket = bucket;
    }

    @Override
    public String bucket() {
        return bucket;
    }
}
