# kotlin-db-migration

## Trait
1. simple & light
1. name based version
1. support dry run
1. support code migration and sql migration

## With Spring
```kotlin
@Component
class Migration: InitializingBean, ApplicationListener<ApplicationReadyEvent>{
    @Value("\${spring.datasource.url}")
    val dbUrl: String = ""

    @Value("\${spring.datasource.username}")
    val dbUsername: String = ""

    @Value("\${spring.datasource.password}")
    val dbPassword: String = ""

    @Autowired
    lateinit var migrationConfig: MigrationConfigProperties

    @Autowired
    protected var context: ApplicationContext? = null

    override fun afterPropertiesSet() {
        if (migrationConfig.isEnable) {
            Migrate(dbUrl, dbUsername, dbPassword).run(migrationConfig.isDryRun)
        }
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        Migrate(dbUrl, dbUsername, dbPassword).runCode(context, migrationConfig.isDryRun)
    }
}
```

```kotlin
class M2018_03_15_105104_test_migration: CodeMigration<ApplicationContext> {
    override fun run(context: ApplicationContext) {
        // context.getBean("xxx")
        // do what every you want
    }
}
```

## Gradle task help create migration
```gradle
    // ./gradlew createMigration -Ptp=code -Pmn=create_role_table
    createMigration.doLast {
        if (project.hasProperty('mn')) {
            def content = "\n"
            def fileName = ""
            def path = ""

            if (project.hasProperty('tp') && project.tp == 'code') {
                def className = "M" + (new Date()).format('yyyy_MM_dd_HHmmss_') + "${project.mn}"
                fileName = "${className}.kt"
                path = "$projectDir/src/main/kotlin/db/migrations"
                content = """
package db.migrations

import info.purocean.dbmigration.CodeMigration
import org.springframework.context.ApplicationContext

class ${className}: CodeMigration<ApplicationContext> {
    override fun run(context: ApplicationContext) {
        // TODO
    }
}
"""
            } else {
                fileName = (new Date()).format('yyyy_MM_dd_HHmmss_') + "${project.mn}.sql"
                path = "$projectDir/src/main/resources/db/migrations"
            }

            new File(path).mkdirs()

            def file = new File("$path/$fileName")

            file.createNewFile()

            file.text = content

            println("created：$path/$fileName")
        } else {
            println("no name!")
        }
    }

```
