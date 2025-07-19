-- 设备类型表
CREATE TABLE device_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- CAMERA, SENSOR, ACCESS_CONTROL, ALARM
    capabilities JSONB, -- 设备能力配置
    default_config JSONB, -- 默认配置
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- 设备表
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    device_type_id BIGINT NOT NULL,
    serial_number VARCHAR(100) UNIQUE,
    model VARCHAR(100),
    manufacturer VARCHAR(100),
    firmware_version VARCHAR(50),
    ip_address INET,
    mac_address MACADDR,
    port INTEGER,
    protocol VARCHAR(20), -- HTTP, RTSP, ONVIF, SNMP
    username VARCHAR(100),
    password_hash VARCHAR(255),
    location_name VARCHAR(200),
    location_coordinates POINT,
    zone VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE', -- ONLINE, OFFLINE, ERROR, MAINTENANCE
    health_score INTEGER DEFAULT 100, -- 0-100
    last_heartbeat TIMESTAMP,
    configuration JSONB,
    metadata JSONB,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (device_type_id) REFERENCES device_types(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 设备组表
CREATE TABLE device_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    path VARCHAR(500), -- 层级路径，如 /root/building1/floor1
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES device_groups(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 设备组关联表
CREATE TABLE device_group_members (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES device_groups(id) ON DELETE CASCADE,
    UNIQUE(device_id, group_id)
);

-- 设备状态历史表
CREATE TABLE device_status_history (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    previous_status VARCHAR(20),
    health_score INTEGER,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- 设备指标表（时序数据）
CREATE TABLE device_metrics (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20),
    tags JSONB,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- 设备维护记录表
CREATE TABLE device_maintenance (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL, -- SCHEDULED, EMERGENCY, REPAIR
    title VARCHAR(200) NOT NULL,
    description TEXT,
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PLANNED', -- PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    performed_by BIGINT,
    notes TEXT,
    cost DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- 创建索引
CREATE INDEX idx_devices_type ON devices(device_type_id);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_ip ON devices(ip_address);
CREATE INDEX idx_devices_location ON devices(location_name);
CREATE INDEX idx_devices_zone ON devices(zone);
CREATE INDEX idx_devices_heartbeat ON devices(last_heartbeat);

CREATE INDEX idx_device_groups_parent ON device_groups(parent_id);
CREATE INDEX idx_device_groups_path ON device_groups(path);

CREATE INDEX idx_device_group_members_device ON device_group_members(device_id);
CREATE INDEX idx_device_group_members_group ON device_group_members(group_id);

CREATE INDEX idx_device_status_history_device ON device_status_history(device_id);
CREATE INDEX idx_device_status_history_created ON device_status_history(created_at);

CREATE INDEX idx_device_metrics_device ON device_metrics(device_id);
CREATE INDEX idx_device_metrics_name ON device_metrics(metric_name);
CREATE INDEX idx_device_metrics_timestamp ON device_metrics(timestamp);
CREATE INDEX idx_device_metrics_device_name_time ON device_metrics(device_id, metric_name, timestamp);

CREATE INDEX idx_device_maintenance_device ON device_maintenance(device_id);
CREATE INDEX idx_device_maintenance_status ON device_maintenance(status);
CREATE INDEX idx_device_maintenance_scheduled ON device_maintenance(scheduled_at);

-- 创建更新时间触发器
CREATE TRIGGER update_device_types_updated_at BEFORE UPDATE ON device_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_devices_updated_at BEFORE UPDATE ON devices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_device_groups_updated_at BEFORE UPDATE ON device_groups
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_device_maintenance_updated_at BEFORE UPDATE ON device_maintenance
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();