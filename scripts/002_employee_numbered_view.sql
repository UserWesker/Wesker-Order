-- 在已创建员工表的数据库执行。employee_no 是根据当前员工记录计算的连续序号。
-- employee_id 是员工表的主键，供登录、关联记录和定位员工时使用。
CREATE OR REPLACE SQL SECURITY INVOKER VIEW employee_numbered AS
SELECT ROW_NUMBER() OVER (ORDER BY e.id) AS employee_no,
       e.id AS employee_id,
       e.name,
       e.username,
       e.status
FROM employee e
ORDER BY employee_no;
