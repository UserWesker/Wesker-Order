-- 在项目使用的 MySQL 数据库执行。先执行 004_create_category.sql。
CREATE TABLE IF NOT EXISTS dish (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    image_key VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    status INT NOT NULL DEFAULT 0 CHECK (status IN (0, 1)),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL,
    KEY idx_dish_category_id (category_id),
    CONSTRAINT fk_dish_category FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dish_flavor (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dish_id BIGINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    value VARCHAR(255) NOT NULL,
    KEY idx_dish_flavor_dish_id (dish_id),
    CONSTRAINT fk_dish_flavor_dish FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
