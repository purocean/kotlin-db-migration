package info.purocean.dbmigration.test

import org.junit.Test
import info.purocean.dbmigration.Migrate

class MigrationTest {
    @Test
    fun runTest () {
        var obj = this.getObj()
        obj.run()
        this.getObj().runCode("jkj")
    }

    private fun getObj () : Migrate {
        return Migrate("jdbc:mysql://localhost/db_migration?useUnicode=true&characterEncoding=utf8&serverTimezone=PRC&useSSL=true", "root", "yang")
    }
}
