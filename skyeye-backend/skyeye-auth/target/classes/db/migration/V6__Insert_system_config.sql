-- 插入默认系统配置
INSERT INTO system_configurations (config_key, config_value, config_type, category, description, is_system) VALUES
-- 系统基础配置
('system.name', 'SkyEye 智能监控系统', 'STRING', 'SYSTEM', '系统名称', true),
('system.version', '1.0.0', 'STRING', 'SYSTEM', '系统版本', true),
('system.timezone', 'Asia/Shanghai', 'STRING', 'SYSTEM', '系统时区', false),
('system.language', 'zh-CN', 'STRING', 'SYSTEM', '系统默认语言', false),

-- 安全配置
('security.password.min_length', '8', 'INTEGER', 'SECURITY', '密码最小长度', false),
('security.password.require_special_char', 'true', 'BOOLEAN', 'SECURITY', '密码是否需要特殊字符', false),
('security.session.timeout', '86400', 'INTEGER', 'SECURITY', '会话超时时间（秒）', false),
('security.login.max_attempts', '5', 'INTEGER', 'SECURITY', '最大登录尝试次数', false),
('security.login.lockout_duration', '1800', 'INTEGER', 'SECURITY', '账户锁定时长（秒）', false),

-- 存储配置
('storage.video.retention_days', '30', 'INTEGER', 'STORAGE', '视频保留天数', false),
('storage.log.retention_days', '90', 'INTEGER', 'STORAGE', '日志保留天数', false),
('storage.max_file_size', '104857600', 'INTEGER', 'STORAGE', '最大文件上传大小（字节）', false),
('storage.allowed_file_types', 'jpg,jpeg,png,mp4,avi,mov', 'STRING', 'STORAGE', '允许的文件类型', false),

-- 报警配置
('alert.default_severity', 'MEDIUM', 'STRING', 'ALERT', '默认报警级别', false),
('alert.auto_acknowledge_timeout', '3600', 'INTEGER', 'ALERT', '自动确认超时时间（秒）', false),
('alert.escalation_timeout', '7200', 'INTEGER', 'ALERT', '报警升级超时时间（秒）', false),
('alert.max_notifications_per_hour', '10', 'INTEGER', 'ALERT', '每小时最大通知数量', false),

-- AI分析配置
('ai.face_recognition.confidence_threshold', '0.8', 'STRING', 'AI', '人脸识别置信度阈值', false),
('ai.behavior_analysis.enabled', 'true', 'BOOLEAN', 'AI', '是否启用行为分析', false),
('ai.object_detection.confidence_threshold', '0.7', 'STRING', 'AI', '目标检测置信度阈值', false),
('ai.analysis.batch_size', '10', 'INTEGER', 'AI', 'AI分析批处理大小', false),

-- 视频配置
('video.stream.default_quality', 'HD', 'STRING', 'VIDEO', '默认视频质量', false),
('video.recording.auto_start', 'false', 'BOOLEAN', 'VIDEO', '是否自动开始录制', false),
('video.stream.max_concurrent', '50', 'INTEGER', 'VIDEO', '最大并发流数量', false),
('video.snapshot.interval', '300', 'INTEGER', 'VIDEO', '快照间隔时间（秒）', false),

-- 设备配置
('device.heartbeat.interval', '60', 'INTEGER', 'DEVICE', '设备心跳间隔（秒）', false),
('device.offline.threshold', '300', 'INTEGER', 'DEVICE', '设备离线阈值（秒）', false),
('device.health.check_interval', '600', 'INTEGER', 'DEVICE', '设备健康检查间隔（秒）', false),
('device.auto_discovery.enabled', 'true', 'BOOLEAN', 'DEVICE', '是否启用设备自动发现', false),

-- 通知配置
('notification.email.enabled', 'true', 'BOOLEAN', 'NOTIFICATION', '是否启用邮件通知', false),
('notification.sms.enabled', 'false', 'BOOLEAN', 'NOTIFICATION', '是否启用短信通知', false),
('notification.push.enabled', 'true', 'BOOLEAN', 'NOTIFICATION', '是否启用推送通知', false),
('notification.retry.max_attempts', '3', 'INTEGER', 'NOTIFICATION', '通知重试最大次数', false),

-- 性能配置
('performance.cache.ttl', '3600', 'INTEGER', 'PERFORMANCE', '缓存生存时间（秒）', false),
('performance.db.connection_pool_size', '20', 'INTEGER', 'PERFORMANCE', '数据库连接池大小', false),
('performance.api.rate_limit', '1000', 'INTEGER', 'PERFORMANCE', 'API速率限制（每分钟）', false),
('performance.log.level', 'INFO', 'STRING', 'PERFORMANCE', '日志级别', false);

-- 插入默认调度任务
INSERT INTO scheduled_tasks (name, description, task_type, cron_expression, parameters, created_by) VALUES
('设备状态检查', '定期检查所有设备的在线状态和健康状况', 'MAINTENANCE', '0 */5 * * * ?', '{"check_type": "health", "timeout": 30}', 1),
('日志清理', '清理过期的系统日志和操作日志', 'MAINTENANCE', '0 0 2 * * ?', '{"retention_days": 90}', 1),
('数据统计', '生成每日数据统计报表', 'ANALYSIS', '0 0 1 * * ?', '{"stat_types": ["device", "alert", "user"]}', 1),
('系统备份', '备份系统配置和重要数据', 'BACKUP', '0 0 3 * * SUN', '{"backup_type": "full", "compress": true}', 1),
('性能监控', '收集系统性能指标', 'DATA_COLLECTION', '0 */10 * * * ?', '{"metrics": ["cpu", "memory", "disk", "network"]}', 1);

-- 记录系统初始化事件
INSERT INTO system_events (event_type, event_category, severity, title, description, source) VALUES
('SYSTEM_INIT', 'SYSTEM', 'INFO', '系统初始化完成', '数据库结构创建完成，默认数据插入成功', 'DATABASE_MIGRATION'),
('USER_CREATED', 'SECURITY', 'INFO', '默认管理员创建', '系统默认管理员账户创建成功', 'SYSTEM_INIT'),
('CONFIG_LOADED', 'SYSTEM', 'INFO', '系统配置加载', '默认系统配置参数加载完成', 'SYSTEM_INIT');