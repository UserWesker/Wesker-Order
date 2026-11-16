-- 已有旧版 employee 表时执行一次，删除员工密码列。
ALTER TABLE employee DROP COLUMN password;
