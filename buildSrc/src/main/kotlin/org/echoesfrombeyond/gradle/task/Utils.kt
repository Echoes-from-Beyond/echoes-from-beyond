package org.echoesfrombeyond.gradle.task

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.SortedSet
import kotlin.io.readText

data class PackageInfo(val name: String, val path: Path) : Comparable<PackageInfo> {
    override fun compareTo(other: PackageInfo): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        return other is PackageInfo && name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

fun readPackagesFile(file: File): SortedSet<PackageInfo> {
    return file.readText(Charsets.UTF_8)
        .splitToSequence('\n')
        .filter(String::isNotBlank)
        .map { line ->
            val path = line.splitToSequence('.').fold(Paths.get("")) { path, name ->
                path.resolve(name)
            }

            PackageInfo(line, path)
        }
        .toSortedSet()
}