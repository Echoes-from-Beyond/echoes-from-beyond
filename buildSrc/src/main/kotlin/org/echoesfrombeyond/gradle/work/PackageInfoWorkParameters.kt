package org.echoesfrombeyond.gradle.work

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

interface PackageInfoWorkParameters : WorkParameters {
    val targetFile: RegularFileProperty
    val contents: Property<String>
}