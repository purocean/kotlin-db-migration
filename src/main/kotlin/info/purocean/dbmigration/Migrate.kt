package info.purocean.dbmigration

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.nio.file.FileSystems
import java.io.IOException
import java.nio.file.FileSystem
import java.util.*
import java.util.HashMap

class Migrate(dbUrl: String, dbUsername: String, dbPassword: String, private var migrationPackage: String = "db.migrations") {
    private var db: DB = DB(dbUrl, dbUsername, dbPassword)

    fun run () {
        val newList = this.getNew()
        println("Migrate ----------- start ${newList.size} ------------")

        newList.forEach { this.runMigration(it) }

        println("Migrate ----------- end ------------")

        this.getHistory().forEach {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            println("${simpleDateFormat.format(it.appliedAt)}\t${it.name}")
        }

        this.db.close()
    }

    fun runCode (context: Any? = null) {
        val newList = this.getNew(Migration.TYPE.CODE)
        println("Migrate Code ----------- start ${newList.size} ------------")

        newList.forEach { this.runCodeMigration(it, context) }

        println("Migrate Code ----------- end ------------")

        this.getHistory().forEach {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            println("${simpleDateFormat.format(it.appliedAt)}\t${it.name}")
        }

        this.db.close()
    }

    @Throws(IOException::class)
    private fun initFileSystem(uri: URI) {
        try {
            FileSystems.newFileSystem(uri, HashMap<String, String>())
        } catch (e: Exception) {
            FileSystems.getDefault()
        }
    }

    private fun runMigration(migration: Migration) {
        println("Migrate ----------- ${migration.name} ------------")

        val uri = URI(migration.uri)
        this.initFileSystem(uri)
        val content = String(Files.readAllBytes(Paths.get(uri)))

        this.db.runMigration(migration, { stmt ->
            stmt.execute(content)
            true
        })
    }

    private fun runCodeMigration(migration: Migration, context: Any?) {
        println("Migrate Code ----------- ${migration.name} ------------")

        val cls = Class.forName(migration.name)

        val obj = cls.newInstance()

        cls.getMethod("run", Object::class.java).invoke(obj, context)

        this.db.writeRecord(migration)
    }

    private fun getHistory (): List<Migration> {
        return this.db.fetchAll()
    }

    private fun getNew (type: Migration.TYPE = Migration.TYPE.SQL): List<Migration> {
        val history = this.getHistory()

        val migrations = ArrayList<Migration>()

        // 查找 sql 迁移
        ClasspathHelper.forPackage(this.migrationPackage).forEach { url ->
            val paths = Reflections(ConfigurationBuilder()
                    .setScanners(ResourcesScanner())
                    .setUrls(url))
                    .getResources(Pattern.compile(".*\\.sql"))
                    .map { Migration(it.substringAfterLast('/'), url.toString() + it, null) }
                    .filter { migration ->
                        history.find { it.name == migration.name } === null
                    }


            migrations.addAll(paths)
        }

        // 查找代码迁移
        ClasspathHelper.forPackage(this.migrationPackage).forEach { url ->
            val paths = Reflections(ConfigurationBuilder()
                    .setScanners(SubTypesScanner())
                    .setUrls(url))
                    .getSubTypesOf(CodeMigration::class.java)
                    .map { Migration(it.name, url.toString() + ':' + it.name, null, Migration.TYPE.CODE) }
                    .filter { migration ->
                        history.find { it.name == migration.name } === null
                    }

            migrations.addAll(paths)
        }


        migrations.sortBy {
            it.name
        }

        migrations.forEach { println(it.uri) }

        return migrations.filter { it.type == type }
    }

    init {
        this.db.createScheme()
    }
}
