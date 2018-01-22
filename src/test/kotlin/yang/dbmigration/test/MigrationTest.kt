package yang.dbmigration.test

import org.junit.Test
import yang.dbmigration.Migrate
import yang.dbmigration.Migration

class MigrationTest {
    @Test
    fun runTest () {
        var obj = this.getObj()
        obj.run()
    }

    private fun getObj () : Migrate {
        return Migrate("jdbc:mysql://localhost/db_migration?useUnicode=true&characterEncoding=utf8&serverTimezone=PRC&useSSL=true", "root", "yang")
    }
}
