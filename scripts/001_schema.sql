-- 先选中自己创建的 MySQL 8.0 数据库，再运行此脚本。
CREATE TABLE IF NOT EXISTS admin_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(32) NOT NULL,
    password VARCHAR(64) NOT NULL,
    status INT NOT NULL CHECK (status IN (0, 1)),
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32),
    username VARCHAR(32) NOT NULL,
    password VARCHAR(64),
    phone VARCHAR(11),
    sex VARCHAR(2),
    id_number VARCHAR(18),
    status INT NOT NULL CHECK (status IN (0, 1)),
    create_time DATETIME,
    update_time DATETIME,
    create_user BIGINT,
    update_user BIGINT,
    UNIQUE KEY uk_employee_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
