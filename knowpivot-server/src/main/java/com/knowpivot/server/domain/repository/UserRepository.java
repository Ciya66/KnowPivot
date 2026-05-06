package com.knowpivot.server.domain.repository;

import com.knowpivot.server.domain.entity.User;

public interface UserRepository {

    User findById(Long id);

    User findByUsername(String username);

    void save(User user);

    void updateById(User user);

    boolean existsByUsername(String username);
}
