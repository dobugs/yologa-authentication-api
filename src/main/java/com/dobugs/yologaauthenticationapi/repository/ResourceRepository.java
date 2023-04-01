package com.dobugs.yologaauthenticationapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dobugs.yologaauthenticationapi.domain.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
