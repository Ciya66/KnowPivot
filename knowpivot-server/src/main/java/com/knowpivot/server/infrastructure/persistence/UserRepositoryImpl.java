package com.knowpivot.server.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowpivot.server.domain.entity.User;
import com.knowpivot.server.domain.repository.UserRepository;
import com.knowpivot.server.infrastructure.persistence.converter.POConverter;
import com.knowpivot.server.infrastructure.persistence.mapper.UserMapper;
import com.knowpivot.server.infrastructure.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final POConverter converter;

    @Override
    public User findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return converter.toUser(po);
    }

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        UserPO po = userMapper.selectOne(wrapper);
        return converter.toUser(po);
    }

    @Override
    public void save(User user) {
        UserPO po = converter.toPO(user);
        userMapper.insert(po);
        user.setId(po.getId());
    }

    @Override
    public void updateById(User user) {
        UserPO po = converter.toPO(user);
        userMapper.updateById(po);
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPO::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }
}
