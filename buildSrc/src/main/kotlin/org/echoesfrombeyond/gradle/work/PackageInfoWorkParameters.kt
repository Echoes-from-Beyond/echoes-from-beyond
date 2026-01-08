package org.echoesfrombeyond.gradle.work

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters

/**
 * Parameters for [GeneratePackageInfoWorkAction].
 */
interface PackageInfoWorkParameters : WorkParameters {
    /**
     * The file we should generate if necessary.
     */
    val targetFile: RegularFileProperty

    /**
     * The desired contents of the file. This may differ from the actual current contents.
     */
    val contents: Property<String>
}