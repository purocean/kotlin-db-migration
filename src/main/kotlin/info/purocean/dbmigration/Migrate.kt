package info.purocean.dbmigration

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern

class Migrate(dbUrl: String, dbUsername: String, dbPassword: String, private var migrationPackage: String = "db.migrations") {
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
        val fileList = ArrayList<String>()

        ClasspathHelper.forPackage(this.migrationPackage).forEach { url ->
            val paths = Reflections(ConfigurationBuilder()
                    .setScanners(ResourcesScanner())
                    .setUrls(url))
                    .getResources(Pattern.compile(".*\\.sql"))
                    .map { url.toString() + it }

            fileList.addAll(paths)
        }

        fileList.sort()

        return fileList.map {
            Migration(it.substringAfterLast('/'), it, null)
        }
    }

    init {
        this.db.createScheme()
    }
}
