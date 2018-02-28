package info.purocean.dbmigration

interface CodeMigration <in T> {
    fun run(context: T)
}