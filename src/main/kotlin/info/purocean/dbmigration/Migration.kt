package info.purocean.dbmigration

import java.util.*

class Migration(var name: String,
                var uri: String,
                var appliedAt: Date? = null,
                var type: TYPE = TYPE.SQL) {
    enum class TYPE(var type: Int) {
        SQL(0),
        CODE(1)
    }
}