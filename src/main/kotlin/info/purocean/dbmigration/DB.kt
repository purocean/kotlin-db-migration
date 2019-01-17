package info.purocean.dbmigration

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.util.*

class DB(dbUrl: String, dbUsername: String, dbPassword: String) {
    private var conn: Connection
    private var migrationTable = "migrations"

    init {
        Class.forName("com.mysql.cj.jdbc.Driver")
        var url = dbUrl
        if (!url.contains("allowMultiQueries", true)) {
            url = url.trim('&') + "&allowMultiQueries=true" // 支持多条sql
        }
        this.conn = DriverManager.getConnection(url, dbUsername, dbPassword)
    }

    fun exec (sql: String) {
        this.conn.autoCommit = false
        val stmt = this.conn.createStatement()

        try {
            println("Migrate ----------- 执行SQL ------------")
            println(sql)

            stmt.execute(sql)
            this.conn.commit()
        } catch (e: Exception) {
            this.conn.rollback()
            e.printStackTrace()
            throw e
        } finally {
            stmt.close()
            this.conn.autoCommit = true
        }
    }

    fun createScheme () {
        val stmt = this.getStmt()

        stmt.execute("CREATE TABLE IF NOT EXISTS `$migrationTable` (\n" +
                "          `name` varchar(180),\n" +
                "          `uri` varchar(255),\n" +
                "          `applied_at` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                "          PRIMARY KEY (`name`)\n" +
                "        ) ENGINE=InnoDB DEFAULT CHARSET=utf8")
        stmt.close()
    }

    fun fetchAll (): List<Migration> {
        val stmt = this.getStmt()
        val result = stmt.executeQuery("select * from $migrationTable where 1 order by applied_at")

        val data = ArrayList<Migration>()
        while (result.next()) {
            val migration = Migration(
                    result.getString("name"),
                    result.getString("uri"),
                    Date(result.getTimestamp("applied_at").time)
            )
            data.add(migration)
        }

        stmt.close()

        return data
    }

    fun runMigration (migration: Migration, call: (stmt: Statement) -> Boolean) {
        this.conn.autoCommit = false
        val stmt = this.conn.createStatement()

        try {
            if (!call(stmt)) {
                throw Exception("执行迁移错误")
            }

            this.writeRecord(migration)

            this.conn.commit()
        } catch (e: Exception) {
            println("Migrate ----------- 执行迁移错误 [${migration.name}] ------------")
            this.conn.rollback()
            e.printStackTrace()
            throw e
        } finally {
            stmt.close()
            this.conn.autoCommit = true
        }
    }

    fun writeRecord (migration: Migration) {
        val stmt = this.conn.prepareStatement("insert into `$migrationTable`" +
                "set name = ?, uri = ?")

        stmt.setString(1, migration.name)
        stmt.setString(2, migration.uri)

        stmt.execute()
        stmt.close()
    }

    fun close () {
        println("Migrate ----------- close connection ------------")
        this.conn.close()
    }

    fun getConnection (): Connection {
        return this.conn
    }

    private fun getStmt (): Statement {
        return this.conn.createStatement()
    }
}
