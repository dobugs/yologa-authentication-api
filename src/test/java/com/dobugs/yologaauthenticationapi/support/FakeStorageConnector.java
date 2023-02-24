package com.dobugs.yologaauthenticationapi.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.dobugs.yologaauthenticationapi.support.dto.response.ResourceResponse;

public class FakeStorageConnector implements StorageConnector {

    private static final String DIRECTORY_REGEX = "/";

    private final Map<String, MultipartFile> storage = new HashMap<>();

    @Override
    public ResourceResponse save(final MultipartFile resource, final String path, final String resourceName) {
        final String resourceKey = concatResourceKey(path, resourceName);
        final String resourceUrl = "http://localhost:8080/" + resourceKey;
        storage.put(resourceKey, resource);
        return new ResourceResponse(resourceKey, resourceUrl);
    }

    @Override
    public void delete(final String resourceKey) {
        storage.remove(resourceKey);
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
}
