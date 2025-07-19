-- 插入默认角色
INSERT INTO roles (name, display_name, description, is_system) VALUES
('SYSTEM_ADMIN', '系统管理员', '拥有系统所有权限的超级管理员', true),
('SECURITY_ADMIN', '安全管理员', '负责安全策略和报警管理的管理员', true),
('OPERATOR', '操作员', '负责日常监控操作的用户', true),
('VIEWER', '观察员', '只能查看监控信息的用户', true);

-- 插入权限
INSERT INTO permissions (name, display_name, description, resource, action) VALUES
-- 用户管理权限
('USER_CREATE', '创建用户', '创建新用户账户', 'USER', 'CREATE'),
('USER_READ', '查看用户', '查看用户信息', 'USER', 'READ'),
('USER_UPDATE', '更新用户', '修改用户信息', 'USER', 'UPDATE'),
('USER_DELETE', '删除用户', '删除用户账户', 'USER', 'DELETE'),
('USER_MANAGE_ROLES', '管理用户角色', '分配和撤销用户角色', 'USER', 'MANAGE_ROLES'),

-- 设备管理权限
('DEVICE_CREATE', '添加设备', '添加新监控设备', 'DEVICE', 'CREATE'),
('DEVICE_READ', '查看设备', '查看设备信息和状态', 'DEVICE', 'READ'),
('DEVICE_UPDATE', '更新设备', '修改设备配置', 'DEVICE', 'UPDATE'),
('DEVICE_DELETE', '删除设备', '删除监控设备', 'DEVICE', 'DELETE'),
('DEVICE_CONTROL', '控制设备', '远程控制设备操作', 'DEVICE', 'CONTROL'),

-- 视频监控权限
('VIDEO_VIEW', '查看视频', '查看实时和历史视频', 'VIDEO', 'VIEW'),
('VIDEO_RECORD', '录制视频', '手动录制视频', 'VIDEO', 'RECORD'),
('VIDEO_DOWNLOAD', '下载视频', '下载视频文件', 'VIDEO', 'DOWNLOAD'),
('VIDEO_MANAGE', '管理视频', '管理视频存储和配置', 'VIDEO', 'MANAGE'),

-- 报警管理权限
('ALERT_VIEW', '查看报警', '查看报警信息', 'ALERT', 'VIEW'),
('ALERT_ACKNOWLEDGE', '确认报警', '确认和处理报警', 'ALERT', 'ACKNOWLEDGE'),
('ALERT_MANAGE', '管理报警', '管理报警规则和配置', 'ALERT', 'MANAGE'),
('ALERT_RESOLVE', '解决报警', '标记报警为已解决', 'ALERT', 'RESOLVE'),

-- AI分析权限
('AI_VIEW', 'AI分析查看', '查看AI分析结果', 'AI', 'VIEW'),
('AI_CONFIGURE', 'AI分析配置', '配置AI分析规则', 'AI', 'CONFIGURE'),

-- 数据分析权限
('ANALYTICS_VIEW', '数据分析查看', '查看数据分析报表', 'ANALYTICS', 'VIEW'),
('ANALYTICS_EXPORT', '数据导出', '导出分析数据', 'ANALYTICS', 'EXPORT'),

-- 系统管理权限
('SYSTEM_CONFIG', '系统配置', '修改系统配置', 'SYSTEM', 'CONFIG'),
('SYSTEM_MONITOR', '系统监控', '查看系统运行状态', 'SYSTEM', 'MONITOR'),
('SYSTEM_LOGS', '系统日志', '查看系统日志', 'SYSTEM', 'LOGS'),

-- 任务管理权限
('TASK_CREATE', '创建任务', '创建调度任务', 'TASK', 'CREATE'),
('TASK_VIEW', '查看任务', '查看任务状态', 'TASK', 'VIEW'),
('TASK_MANAGE', '管理任务', '管理任务执行', 'TASK', 'MANAGE');

-- 为系统管理员分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SYSTEM_ADMIN';

-- 为安全管理员分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SECURITY_ADMIN'
AND p.name IN (
    'USER_READ', 'DEVICE_READ', 'DEVICE_UPDATE', 'DEVICE_CONTROL',
    'VIDEO_VIEW', 'VIDEO_RECORD', 'VIDEO_MANAGE',
    'ALERT_VIEW', 'ALERT_ACKNOWLEDGE', 'ALERT_MANAGE', 'ALERT_RESOLVE',
    'AI_VIEW', 'AI_CONFIGURE',
    'ANALYTICS_VIEW', 'ANALYTICS_EXPORT',
    'SYSTEM_MONITOR', 'SYSTEM_LOGS',
    'TASK_VIEW', 'TASK_MANAGE'
);

-- 为操作员分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OPERATOR'
AND p.name IN (
    'DEVICE_READ', 'DEVICE_CONTROL',
    'VIDEO_VIEW', 'VIDEO_RECORD',
    'ALERT_VIEW', 'ALERT_ACKNOWLEDGE', 'ALERT_RESOLVE',
    'AI_VIEW',
    'ANALYTICS_VIEW',
    'TASK_VIEW'
);

-- 为观察员分配权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'VIEWER'
AND p.name IN (
    'DEVICE_READ',
    'VIDEO_VIEW',
    'ALERT_VIEW',
    'AI_VIEW',
    'ANALYTICS_VIEW'
);

-- 创建默认管理员用户
INSERT INTO users (username, email, password_hash, full_name, status)
VALUES ('admin', 'admin@skyeye.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRJ.9Omu/3u', '系统管理员', 'ACTIVE');
-- 密码是: admin123

-- 为默认管理员分配系统管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'SYSTEM_ADMIN';

-- 插入默认设备类型
INSERT INTO device_types (name, display_name, description, category, capabilities) VALUES
('IP_CAMERA', 'IP摄像头', '网络摄像头设备', 'CAMERA', '{"video": true, "audio": false, "ptz": false, "night_vision": true}'),
('PTZ_CAMERA', 'PTZ摄像头', '支持云台控制的摄像头', 'CAMERA', '{"video": true, "audio": true, "ptz": true, "night_vision": true, "zoom": true}'),
('DOOR_SENSOR', '门磁传感器', '门窗开关状态传感器', 'SENSOR', '{"contact": true, "tamper": true}'),
('MOTION_SENSOR', '运动传感器', '人体运动检测传感器', 'SENSOR', '{"motion": true, "temperature": false}'),
('ACCESS_CONTROLLER', '门禁控制器', '门禁系统控制设备', 'ACCESS_CONTROL', '{"card_reader": true, "relay": true, "wiegand": true}'),
('ALARM_PANEL', '报警主机', '安防报警系统主机', 'ALARM', '{"zones": 8, "sirens": 2, "communication": ["ethernet", "wifi"]});

-- 插入默认报警类型
INSERT INTO alert_types (name, display_name, description, category, severity) VALUES
('UNAUTHORIZED_ACCESS', '未授权访问', '检测到未授权人员访问', 'SECURITY', 'HIGH'),
('ABNORMAL_BEHAVIOR', '异常行为', 'AI检测到异常行为模式', 'AI_ANALYSIS', 'MEDIUM'),
('DEVICE_OFFLINE', '设备离线', '监控设备失去连接', 'DEVICE', 'MEDIUM'),
('DEVICE_ERROR', '设备故障', '设备运行异常或故障', 'DEVICE', 'HIGH'),
('FACE_RECOGNITION', '人脸识别', '人脸识别匹配结果', 'AI_ANALYSIS', 'LOW'),
('INTRUSION_DETECTION', '入侵检测', '检测到入侵行为', 'SECURITY', 'CRITICAL'),
('SYSTEM_ERROR', '系统错误', '系统运行异常', 'SYSTEM', 'HIGH'),
('LOW_STORAGE', '存储空间不足', '存储空间使用率过高', 'SYSTEM', 'MEDIUM');

-- 插入默认通知渠道
INSERT INTO notification_channels (name, type, configuration, created_by) VALUES
('系统邮箱', 'EMAIL', '{"smtp_host": "localhost", "smtp_port": 587, "from_email": "noreply@skyeye.com"}', 1),
('系统短信', 'SMS', '{"provider": "aliyun", "template_id": "SMS_001"}', 1),
('系统推送', 'PUSH', '{"provider": "firebase", "app_id": "skyeye-app"}', 1);