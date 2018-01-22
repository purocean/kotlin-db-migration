# kotlin-db-migration
数据库迁移

```kotlin

val migrate = Migrate("jdbc:mysql://localhost/db_migration?useUnicode=true&characterEncoding=utf8&serverTimezone=PRC&useSSL=true", "root", "yang")

migrate.run()

```
