-- 在已建有 category 表的 MySQL 数据库执行，以兼容不再提交排序值的分类接口。
ALTER TABLE category ALTER COLUMN sort SET DEFAULT 0;
