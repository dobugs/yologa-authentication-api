package com.dobugs.yologaauthenticationapi.support.resource;

import java.io.IOException;
import java.net.URL;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.support.StorageConnector;
import com.dobugs.yologaauthenticationapi.support.StorageProvider;
import com.dobugs.yologaauthenticationapi.support.dto.response.ResourceResponse;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class S3Connector implements StorageConnector {

    private final S3Template s3Template;
    private final StorageProvider s3Provider;

    @Override
    public ResourceResponse save(final MultipartFile resource, final String path, final String resourceName) {
        final String resourceKey = concatResourceKey(path, resourceName);
        final S3Resource savedResource = s3Template.store(s3Provider.bucket(), resourceKey, resource);
        return createResourceResponse(resourceKey, savedResource);
    }

    @Override
    public void delete(final String resourceUrl) {
        s3Template.deleteObject(resourceUrl);
    }

    private String concatResourceKey(final String path, final String resourceName) {
        if (path.endsWith("/")) {
            return path + resourceName;
        }
        return String.join("/", path, resourceName);
    }

    private ResourceResponse createResourceResponse(final String resourceKey, final S3Resource resource) {
        try {
            final URL resourceURL = resource.getURL();
            return new ResourceResponse(resourceKey, resourceURL.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
