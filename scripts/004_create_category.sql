-- 在项目使用的 MySQL 数据库执行。
CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    type INT NOT NULL CHECK (type IN (1, 2)),
    sort INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 0 CHECK (status IN (0, 1)),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL,
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
