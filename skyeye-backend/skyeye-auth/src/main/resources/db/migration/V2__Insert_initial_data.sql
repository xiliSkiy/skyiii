-- 插入初始角色数据
INSERT INTO roles (name, description) VALUES
('SYSTEM_ADMIN', '系统管理员，拥有系统所有权限'),
('SECURITY_ADMIN', '安全管理员，负责安全相关功能管理'),
('OPERATOR', '操作员，负责日常监控操作'),
('VIEWER', '查看者，只能查看监控画面和报告');

-- 插入初始权限数据
INSERT INTO permissions (name, description, resource, action) VALUES
-- 用户管理权限
('user:create', '创建用户', 'user', 'create'),
('user:read', '查看用户', 'user', 'read'),
('user:update', '更新用户', 'user', 'update'),
('user:delete', '删除用户', 'user', 'delete'),
('user:manage', '管理用户', 'user', 'manage'),

-- 角色权限管理
('role:create', '创建角色', 'role', 'create'),
('role:read', '查看角色', 'role', 'read'),
('role:update', '更新角色', 'role', 'update'),
('role:delete', '删除角色', 'role', 'delete'),
('role:manage', '管理角色', 'role', 'manage'),

-- 设备管理权限
('device:create', '添加设备', 'device', 'create'),
('device:read', '查看设备', 'device', 'read'),
('device:update', '更新设备', 'device', 'update'),
('device:delete', '删除设备', 'device', 'delete'),
('device:control', '控制设备', 'device', 'control'),
('device:manage', '管理设备', 'device', 'manage'),

-- 监控权限
('monitor:view', '查看监控', 'monitor', 'view'),
('monitor:control', '控制监控', 'monitor', 'control'),
('monitor:record', '录制视频', 'monitor', 'record'),

-- 报警权限
('alert:read', '查看报警', 'alert', 'read'),
('alert:handle', '处理报警', 'alert', 'handle'),
('alert:config', '配置报警', 'alert', 'config'),
('alert:manage', '管理报警', 'alert', 'manage'),

-- 数据分析权限
('analytics:read', '查看分析', 'analytics', 'read'),
('analytics:export', '导出数据', 'analytics', 'export'),
('analytics:config', '配置分析', 'analytics', 'config'),

-- 系统配置权限
('system:config', '系统配置', 'system', 'config'),
('system:maintain', '系统维护', 'system', 'maintain'),
('system:monitor', '系统监控', 'system', 'monitor'),

-- 日志权限
('log:read', '查看日志', 'log', 'read'),
('log:export', '导出日志', 'log', 'export'),
('log:manage', '管理日志', 'log', 'manage');

-- 为系统管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SYSTEM_ADMIN';

-- 为安全管理员角色分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SECURITY_ADMIN'
AND p.name IN (
    'user:read', 'user:update', 'user:manage',
    'role:read',
    'device:read', 'device:update', 'device:control', 'device:manage',
    'monitor:view', 'monitor:control', 'monitor:record',
    'alert:read', 'alert:handle', 'alert:config', 'alert:manage',
    'analytics:read', 'analytics:export', 'analytics:config',
    'system:config', 'system:monitor',
    'log:read', 'log:export'
);

-- 为操作员角色分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OPERATOR'
AND p.name IN (
    'device:read', 'device:control',
    'monitor:view', 'monitor:control', 'monitor:record',
    'alert:read', 'alert:handle',
    'analytics:read',
    'log:read'
);

-- 为查看者角色分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'VIEWER'
AND p.name IN (
    'device:read',
    'monitor:view',
    'alert:read',
    'analytics:read'
);

-- 创建默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, full_name, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRs.b7nABSK', 'admin@skyeye.com', '系统管理员', 'ACTIVE');

-- 为默认管理员分配系统管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'SYSTEM_ADMIN';