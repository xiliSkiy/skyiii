package com.skyeye.auth.repository;

import com.skyeye.auth.entity.Role;
import com.skyeye.auth.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(UserRole name);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(UserRole name);
}