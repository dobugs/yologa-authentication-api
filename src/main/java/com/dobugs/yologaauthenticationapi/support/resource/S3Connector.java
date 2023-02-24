package com.dobugs.yologaauthenticationapi.support.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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

    private static final String DIRECTORY_REGEX = "/";

    private final S3Template s3Template;
    private final StorageProvider s3Provider;

    @Override
    public ResourceResponse save(final MultipartFile resource, final String path, final String resourceName) {
        final String resourceKey = concatResourceKey(path, resourceName);
        final ByteArrayInputStream serializedResource = serialize(resource);
        final S3Resource savedResource = s3Template.store(s3Provider.bucket(), resourceKey, serializedResource);
        return createResourceResponse(resourceKey, savedResource);
    }

    @Override
    public void delete(final String resourceKey) {
        s3Template.deleteObject(s3Provider.bucket(), resourceKey);
    }

    private String concatResourceKey(final String path, final String resourceName) {
        final String splitPath = split(path);
        if (splitPath.equals("")) {
            return resourceName;
        }
        return String.join(DIRECTORY_REGEX, splitPath, resourceName);
    }

    private String split(final String path) {
        final List<String> splitPaths = Arrays.stream(path.split(DIRECTORY_REGEX))
            .filter(split -> !split.equals(""))
            .toList();
        return String.join(DIRECTORY_REGEX, splitPaths);
    }

    private ByteArrayInputStream serialize(final MultipartFile resource) {
        try {
            return new ByteArrayInputStream(resource.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
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
