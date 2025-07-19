-- 创建额外的数据库
CREATE DATABASE skyeye_device;
CREATE DATABASE skyeye_video;
CREATE DATABASE skyeye_ai;
CREATE DATABASE skyeye_alert;
CREATE DATABASE skyeye_analytics;
CREATE DATABASE skyeye_task;

-- 为每个数据库创建用户权限
GRANT ALL PRIVILEGES ON DATABASE skyeye_device TO skyeye;
GRANT ALL PRIVILEGES ON DATABASE skyeye_video TO skyeye;
GRANT ALL PRIVILEGES ON DATABASE skyeye_ai TO skyeye;
GRANT ALL PRIVILEGES ON DATABASE skyeye_alert TO skyeye;
GRANT ALL PRIVILEGES ON DATABASE skyeye_analytics TO skyeye;
GRANT ALL PRIVILEGES ON DATABASE skyeye_task TO skyeye;