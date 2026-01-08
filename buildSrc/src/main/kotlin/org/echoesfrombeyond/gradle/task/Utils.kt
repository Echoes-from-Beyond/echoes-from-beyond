package org.echoesfrombeyond.gradle.task

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.SortedSet
import kotlin.io.readText

/**
 * Represents a Java package for which a package-info.java file should potentially be generated.
 *
 * Implements [Comparable]. Instances are compared by [name], not [path]; likewise with equality and
 * naturally [Object.hashCode].
 *
 * @property name the name of the package, like "java.nio.file"
 * @property path the path of the package relative to its source root
 */
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

/**
 * Reads the packages file (see [GeneratePackageTree]) into a [SortedSet] of [PackageInfo].
 *
 * @param file the packages file
 * @return an ordered list of [PackageInfo] objects
 */
fun readPackagesFile(file: File): SortedSet<PackageInfo> {
    // Just read the whole text, the file won't ever be that big.
    return file.readText(Charsets.UTF_8)
        .splitToSequence('\n')
        .filter(String::isNotBlank)
        .map { line ->
            // Convert the package name to the path.
            val path = line.splitToSequence('.').fold(Paths.get("")) { path, name ->
                path.resolve(name)
            }

            PackageInfo(line, path)
        }
        .toSortedSet()
}