package info.purocean.dbmigration

import java.sql.Connection

interface CodeMigration <in T> {
    fun run(context: T, connection: Connection)
}
