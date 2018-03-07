package info.purocean.dbmigration

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import org.reflections.Configuration
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import org.reflections.scanners.Scanner
import org.reflections.scanners.SubTypesScanner

class MyReflections(configuration: Configuration) : Reflections(configuration) {
    override fun expandSuperTypes() {
        val mmap = store.get(index(SubTypesScanner::class.java))
        val keys = Sets.difference(mmap.keySet(), Sets.newHashSet(mmap.values()))
        val expand = HashMultimap.create<String, String>()

        keys.forEach {
            val type = ReflectionUtils.forName(it)
            if (type != null) {
                expandSupertypes(expand, it, type)
            }
        }
    }

    private fun expandSupertypes(mmap: Multimap<String, String>, key: String, type: Class<*>) {
        ReflectionUtils.getAllSuperTypes(type)
                .filter { mmap.put(it.name, key) }
                .forEach { expandSupertypes(mmap, it.name, it) }
    }

    private fun index(scannerClass: Class<out Scanner>): String {
        return scannerClass.simpleName
    }
}