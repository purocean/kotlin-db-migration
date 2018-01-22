package yang.dbmigration

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class Migrate(dbUrl: String, dbUsername: String, dbPassword: String, private var migrationPath: String = "/db/migrations") {
    private var db: DB = DB(dbUrl, dbUsername, dbPassword)

    fun run () {
        val newList = this.getNew()
        println("Migrate ----------- start ${newList.size} ------------")

        newList.forEach { this.runMigration(it) }

        println("Migrate ----------- end ------------")

        this.getHistory().forEach { println("${it.name}\t${it.appliedAt}") }

        this.db.close()
    }

    private fun runMigration(migration: Migration) {
        println("Migrate ----------- ${migration.name} ------------")
        val content = String(Files.readAllBytes(Paths.get(URI(migration.uri))))

        this.db.runMigration(migration, { stmt ->
            stmt.execute(content)
            true
        })
    }

    private fun getHistory (): List<Migration> {
        return this.db.fetchAll()
    }

    private fun getNew (): List<Migration> {
        val path = Migration::class.java.getResource(this.migrationPath)
        if (path === null) {
            throw Exception("迁移目录未找到 [${this.migrationPath}]")
        }

        val dir = File(path.toURI())

        val fileList = dir.listFiles { file ->
            this.getHistory().find { migration ->
                migration.name == file.name
            } === null && file.extension == "sql"
        }

        Arrays.sort(fileList)

        return fileList.map {
            Migration(it.name, it.toURI().toString(), null)
        }
    }

    init {
        this.db.createScheme()
    }
}
