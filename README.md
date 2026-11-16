# 威斯克点单

员工管理练习项目，包含管理员登录、员工新增、分页查询和启用／禁用。员工资料不包含登录密码。运行前请准备 JDK 17、Maven 和自己的 MySQL 8.0 数据库。

如果已有旧版数据库，并且 `employee` 表仍有密码列，请在停掉旧版应用后执行一次 [`scripts/003_remove_employee_password.sql`](scripts/003_remove_employee_password.sql)。

## 接入自己的数据库

1. 在 MySQL 中新建数据库。在 DataGrip 或 MySQL 客户端选中这个库，执行 [`scripts/001_schema.sql`](scripts/001_schema.sql)。如果想在数据库里查看连续的员工序号，再执行 [`scripts/002_employee_numbered_view.sql`](scripts/002_employee_numbered_view.sql)。
2. 把 [`config/employee-login.example.yml`](config/employee-login.example.yml) 复制到项目根目录下的 `.local/employee-login-local.yml`，填入自己的 JDBC 地址、数据库账号和密码，并生成独立的随机 JWT 密钥。PowerShell 可以用下面的命令生成可用的密钥：

   ```powershell
   $key = New-Object byte[] 32
   [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($key)
   [Convert]::ToBase64String($key)
   ```

   也可用 `WESKER_DB_URL`、`WESKER_DB_USER`、`WESKER_DB_PASSWORD` 和 `WESKER_JWT_SECRET` 环境变量提供配置。把个人配置和数据库文件留在本机。

3. 给自己的数据库创建第一个管理员。以下 PowerShell 命令从项目根目录执行，通过终端输入自己选的密码，获得 BCrypt 哈希：

   ```powershell
   mvn package
   Push-Location wesker-server/target
   jar -xf wesker-server-1.0-SNAPSHOT.jar BOOT-INF/lib
   java -cp 'BOOT-INF/lib/*' ../../scripts/GenerateAdminHash.java
   Pop-Location
   ```

   把下面的示例用户名、显示名和哈希换成自己的值，再在自己的数据库中执行 SQL：

   ```sql
   INSERT INTO admin_account (username, name, password, status, create_time, update_time)
   VALUES ('your_admin', '管理员', '替换为上一步生成的BCrypt哈希', 1, NOW(), NOW());
   ```

4. 在 IntelliJ IDEA 打开项目，把运行配置的工作目录设为项目根目录，启动 `WeskerOrderApplication`。打开根目录的 `打开威斯克点单.html`，用刚创建的管理员账号和密码登录。
