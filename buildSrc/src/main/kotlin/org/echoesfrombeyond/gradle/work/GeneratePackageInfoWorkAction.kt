package org.echoesfrombeyond.gradle.work

import org.gradle.workers.WorkAction
import java.security.MessageDigest

private val threadLocalDigest = ThreadLocal.withInitial {
    MessageDigest.getInstance("SHA-1")
}

abstract class GeneratePackageInfoWorkAction : WorkAction<PackageInfoWorkParameters> {
    override fun execute() {
        val contents = parameters.contents.get().toByteArray(Charsets.UTF_8)
        val file = parameters.targetFile.asFile.get()

        val exists = file.exists()

        if (exists && (file.length() == contents.size.toLong())) {
            val md = threadLocalDigest.get()

            md.update(contents)
            val expected = md.digest()

            md.update(file.readBytes())
            val actual = md.digest()

            if (expected.contentEquals(actual)) return
        }

        if (!exists) {
            file.parentFile.mkdirs()
        }

        file.writeBytes(contents)
    }
}