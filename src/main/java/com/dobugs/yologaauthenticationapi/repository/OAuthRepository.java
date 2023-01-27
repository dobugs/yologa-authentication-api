package com.dobugs.yologaauthenticationapi.repository;

import org.springframework.data.repository.CrudRepository;

import com.dobugs.yologaauthenticationapi.domain.OAuthToken;

public interface OAuthRepository extends CrudRepository<OAuthToken, Long> {
}
