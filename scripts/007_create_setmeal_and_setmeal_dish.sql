-- 在项目使用的 MySQL 数据库执行。先执行 004 和 005 建表脚本。
CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    image_key VARCHAR(255),
    description VARCHAR(255),
    status INT NOT NULL DEFAULT 0 CHECK (status IN (0, 1)),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_user BIGINT NOT NULL,
    update_user BIGINT NOT NULL,
    KEY idx_setmeal_category_id (category_id),
    CONSTRAINT fk_setmeal_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- name 和 price 保存加入套餐时的菜品名称和单份价格，copies 是份数。
CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    setmeal_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    name VARCHAR(32) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    copies INT NOT NULL CHECK (copies > 0),
    UNIQUE KEY uk_setmeal_dish_pair (setmeal_id, dish_id),
    KEY idx_setmeal_dish_dish_id (dish_id),
    CONSTRAINT fk_setmeal_dish_setmeal FOREIGN KEY (setmeal_id) REFERENCES setmeal (id) ON DELETE RESTRICT,
    CONSTRAINT fk_setmeal_dish_dish FOREIGN KEY (dish_id) REFERENCES dish (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
