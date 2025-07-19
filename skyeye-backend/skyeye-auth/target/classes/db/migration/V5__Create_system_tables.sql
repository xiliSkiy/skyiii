-- 系统操作日志表
CREATE TABLE operation_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    resource_name VARCHAR(200),
    action VARCHAR(50) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    request_id VARCHAR(100),
    parameters JSONB,
    result VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, ERROR
    error_message TEXT,
    execution_time INTEGER, -- 执行时间（毫秒）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 系统事件日志表
CREATE TABLE system_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    event_category VARCHAR(50) NOT NULL, -- SYSTEM, SECURITY, DEVICE, AI, ALERT
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO', -- CRITICAL, ERROR, WARN, INFO, DEBUG
    title VARCHAR(200) NOT NULL,
    description TEXT,
    source VARCHAR(100),
    source_id VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 任务调度表
CREATE TABLE scheduled_tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    task_type VARCHAR(50) NOT NULL, -- DATA_COLLECTION, MAINTENANCE, BACKUP, ANALYSIS
    cron_expression VARCHAR(100),
    parameters JSONB,
    is_enabled BOOLEAN DEFAULT TRUE,
    last_execution TIMESTAMP,
    next_execution TIMESTAMP,
    execution_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    timeout_seconds INTEGER DEFAULT 3600,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 任务执行历史表
CREATE TABLE task_execution_history (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    execution_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING', -- RUNNING, COMPLETED, FAILED, CANCELLED
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    result_data JSONB,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    FOREIGN KEY (task_id) REFERENCES scheduled_tasks(id) ON DELETE CASCADE
);

-- 系统配置表
CREATE TABLE system_configurations (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_type VARCHAR(20) NOT NULL DEFAULT 'STRING', -- STRING, INTEGER, BOOLEAN, JSON
    category VARCHAR(50) NOT NULL,
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_system BOOLEAN DEFAULT FALSE,
    validation_rule VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 文件存储表
CREATE TABLE file_storage (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    file_hash VARCHAR(64), -- SHA-256 hash
    storage_type VARCHAR(20) DEFAULT 'LOCAL', -- LOCAL, S3, MINIO
    bucket_name VARCHAR(100),
    metadata JSONB,
    uploaded_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- 数据统计表
CREATE TABLE data_statistics (
    id BIGSERIAL PRIMARY KEY,
    stat_date DATE NOT NULL,
    stat_type VARCHAR(50) NOT NULL,
    stat_category VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    dimensions JSONB, -- 维度信息
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(stat_date, stat_type, stat_category, metric_name)
);

-- AI分析结果表
CREATE TABLE ai_analysis_results (
    id BIGSERIAL PRIMARY KEY,
    analysis_type VARCHAR(50) NOT NULL, -- FACE_RECOGNITION, BEHAVIOR_ANALYSIS, OBJECT_DETECTION
    source_type VARCHAR(50) NOT NULL, -- VIDEO_STREAM, IMAGE, VIDEO_FILE
    source_id VARCHAR(100) NOT NULL,
    device_id BIGINT,
    model_name VARCHAR(100),
    model_version VARCHAR(50),
    confidence_score DOUBLE PRECISION,
    analysis_data JSONB NOT NULL, -- 分析结果数据
    bounding_boxes JSONB, -- 边界框信息
    processing_time_ms INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id)
);

-- 创建索引
CREATE INDEX idx_operation_logs_user ON operation_logs(user_id);
CREATE INDEX idx_operation_logs_operation ON operation_logs(operation);
CREATE INDEX idx_operation_logs_resource ON operation_logs(resource_type, resource_id);
CREATE INDEX idx_operation_logs_created ON operation_logs(created_at);
CREATE INDEX idx_operation_logs_result ON operation_logs(result);

CREATE INDEX idx_system_events_type ON system_events(event_type);
CREATE INDEX idx_system_events_category ON system_events(event_category);
CREATE INDEX idx_system_events_severity ON system_events(severity);
CREATE INDEX idx_system_events_created ON system_events(created_at);
CREATE INDEX idx_system_events_source ON system_events(source, source_id);

CREATE INDEX idx_scheduled_tasks_enabled ON scheduled_tasks(is_enabled);
CREATE INDEX idx_scheduled_tasks_next_execution ON scheduled_tasks(next_execution);
CREATE INDEX idx_scheduled_tasks_type ON scheduled_tasks(task_type);

CREATE INDEX idx_task_execution_history_task ON task_execution_history(task_id);
CREATE INDEX idx_task_execution_history_status ON task_execution_history(status);
CREATE INDEX idx_task_execution_history_started ON task_execution_history(started_at);

CREATE INDEX idx_system_configurations_key ON system_configurations(config_key);
CREATE INDEX idx_system_configurations_category ON system_configurations(category);

CREATE INDEX idx_file_storage_hash ON file_storage(file_hash);
CREATE INDEX idx_file_storage_type ON file_storage(storage_type);
CREATE INDEX idx_file_storage_uploaded_by ON file_storage(uploaded_by);
CREATE INDEX idx_file_storage_created ON file_storage(created_at);

CREATE INDEX idx_data_statistics_date ON data_statistics(stat_date);
CREATE INDEX idx_data_statistics_type ON data_statistics(stat_type, stat_category);
CREATE INDEX idx_data_statistics_metric ON data_statistics(metric_name);

CREATE INDEX idx_ai_analysis_results_type ON ai_analysis_results(analysis_type);
CREATE INDEX idx_ai_analysis_results_source ON ai_analysis_results(source_type, source_id);
CREATE INDEX idx_ai_analysis_results_device ON ai_analysis_results(device_id);
CREATE INDEX idx_ai_analysis_results_created ON ai_analysis_results(created_at);
CREATE INDEX idx_ai_analysis_results_confidence ON ai_analysis_results(confidence_score);

-- 创建更新时间触发器
CREATE TRIGGER update_scheduled_tasks_updated_at BEFORE UPDATE ON scheduled_tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_configurations_updated_at BEFORE UPDATE ON system_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();