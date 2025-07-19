-- 报警类型表
CREATE TABLE alert_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- SECURITY, DEVICE, SYSTEM, AI_ANALYSIS
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- CRITICAL, HIGH, MEDIUM, LOW
    default_config JSONB,
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 报警规则表
CREATE TABLE alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    alert_type_id BIGINT NOT NULL,
    conditions JSONB NOT NULL, -- 报警条件配置
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_enabled BOOLEAN DEFAULT TRUE,
    threshold_config JSONB, -- 阈值配置
    time_window INTEGER, -- 时间窗口（秒）
    cooldown_period INTEGER DEFAULT 300, -- 冷却期（秒）
    escalation_config JSONB, -- 升级配置
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (alert_type_id) REFERENCES alert_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 报警事件表
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_rule_id BIGINT,
    alert_type_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, CLOSED
    source_type VARCHAR(50), -- DEVICE, AI_ANALYSIS, SYSTEM, MANUAL
    source_id BIGINT, -- 来源ID（设备ID等）
    device_id BIGINT,
    location_name VARCHAR(200),
    location_coordinates POINT,
    event_data JSONB, -- 事件相关数据
    evidence_urls TEXT[], -- 证据文件URLs
    confidence_score DOUBLE PRECISION, -- AI分析置信度
    acknowledged_by BIGINT,
    acknowledged_at TIMESTAMP,
    assigned_to BIGINT,
    assigned_at TIMESTAMP,
    resolved_by BIGINT,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    escalated_at TIMESTAMP,
    escalated_to BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (alert_rule_id) REFERENCES alert_rules(id),
    FOREIGN KEY (alert_type_id) REFERENCES alert_types(id),
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (acknowledged_by) REFERENCES users(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id),
    FOREIGN KEY (escalated_to) REFERENCES users(id)
);

-- 报警处理历史表
CREATE TABLE alert_history (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATED, ACKNOWLEDGED, ASSIGNED, RESOLVED, ESCALATED, CLOSED
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    performed_by BIGINT,
    notes TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- 通知渠道表
CREATE TABLE notification_channels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- EMAIL, SMS, WEBHOOK, PUSH
    configuration JSONB NOT NULL, -- 渠道配置（邮箱地址、手机号等）
    is_enabled BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 通知规则表
CREATE TABLE notification_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    alert_type_id BIGINT,
    severity_filter VARCHAR(20)[], -- 触发的严重级别
    conditions JSONB, -- 通知条件
    channels BIGINT[] NOT NULL, -- 通知渠道ID数组
    template_config JSONB, -- 通知模板配置
    is_enabled BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (alert_type_id) REFERENCES alert_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 通知发送记录表
CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    recipient VARCHAR(200) NOT NULL,
    subject VARCHAR(200),
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED, DELIVERED
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alert_id) REFERENCES alerts(id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES notification_channels(id)
);

-- 创建索引
CREATE INDEX idx_alert_rules_type ON alert_rules(alert_type_id);
CREATE INDEX idx_alert_rules_enabled ON alert_rules(is_enabled);

CREATE INDEX idx_alerts_rule ON alerts(alert_rule_id);
CREATE INDEX idx_alerts_type ON alerts(alert_type_id);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_device ON alerts(device_id);
CREATE INDEX idx_alerts_source ON alerts(source_type, source_id);
CREATE INDEX idx_alerts_created ON alerts(created_at);
CREATE INDEX idx_alerts_assigned ON alerts(assigned_to);

CREATE INDEX idx_alert_history_alert ON alert_history(alert_id);
CREATE INDEX idx_alert_history_action ON alert_history(action);
CREATE INDEX idx_alert_history_created ON alert_history(created_at);

CREATE INDEX idx_notification_channels_type ON notification_channels(type);
CREATE INDEX idx_notification_channels_enabled ON notification_channels(is_enabled);

CREATE INDEX idx_notification_rules_type ON notification_rules(alert_type_id);
CREATE INDEX idx_notification_rules_enabled ON notification_rules(is_enabled);

CREATE INDEX idx_notification_logs_alert ON notification_logs(alert_id);
CREATE INDEX idx_notification_logs_channel ON notification_logs(channel_id);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_created ON notification_logs(created_at);

-- 创建更新时间触发器
CREATE TRIGGER update_alert_types_updated_at BEFORE UPDATE ON alert_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alert_rules_updated_at BEFORE UPDATE ON alert_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at BEFORE UPDATE ON alerts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_channels_updated_at BEFORE UPDATE ON notification_channels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_rules_updated_at BEFORE UPDATE ON notification_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_logs_updated_at BEFORE UPDATE ON notification_logs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();