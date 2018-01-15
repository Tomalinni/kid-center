package com.joins.kidcenter.service.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

interface StorageProvider {

    /**
     * @return names of files sorted by name in ascending order
     */
    fun listNames(): List<String>

    fun list(): List<File>

    fun filesCount(): Int

    fun exists(fileName: String): Boolean

    /**
     * @return file size in bytes, -1 if file is not found
     */
    fun size(fileName: String): Long

    fun download(fileName: String, output: OutputStream)

    fun upload(fileName: String, input: InputStream)

    fun delete(fileName: String)

    fun deleteRoot()
}

@Component
open class FileStorageServiceImpl {

    @Value("\${app.files.storage.path}")
    var storagePath: String = ""
    @Value("\${app.files.clear.storage.at.shutdown}")
    var clearStorageAtShutdown = false

    var storageInitialized = false
    var storageFolder: File = File("") //init with dummy value

    @PostConstruct
    private fun init() {
        if (storagePath.isBlank()) throw IllegalStateException("File storage is not configured properly. Storage path $storagePath is not valid.")
        storageFolder = File(storagePath)
        if (!storageFolder.exists()) {
            val creationSuccess = storageFolder.mkdirs()
            if (!creationSuccess) {
                throw IllegalStateException("File storage does not exist and can not be created at path $storagePath")
            }
        } else {
            if (storageFolder.isFile) {
                throw IllegalStateException("File storage path $storagePath references file. Path to folder is expected.")
            } else if (!storageFolder.canRead()) {
                throw IllegalStateException("File storage at path $storagePath can not be read.")
            } else if (!storageFolder.canWrite()) {
                throw IllegalStateException("File storage at path $storagePath can not be written in.")
            }
        }
        storageInitialized = true
    }

    @PreDestroy
    private fun destroy() {
        if (clearStorageAtShutdown && storageInitialized) {
            storageFolder.delete()
        }
    }

    fun providers() = Providers(storageFolder)

    class Providers(val storageFolder: File) {

        fun student(studentId: Long) = StorageProviderImpl(studentFilesPath(studentId))

        fun studentRelative(studentId: Long, relativeId: Long) = StorageProviderImpl(studentRelativeFilesPath(relativeId, studentId))

        fun payment(paymentId: Long, fieldId: String) = StorageProviderImpl(paymentFilesPath(paymentId, fieldId))

        fun homework(homeworkId: Long) = StorageProviderImpl(homeworkFilesPath(homeworkId))

        fun lesson(lessonId: String) = StorageProviderImpl(lessonFilesPath(lessonId))

        fun studentCard(cardId: Long) = StorageProviderImpl(studentCardFilesPath(cardId))

        private fun studentRelativeFilesPath(relativeId: Long, studentId: Long) = studentFilesPath(studentId).resolve("_relatives/$relativeId")

        private fun studentFilesPath(studentId: Long) = Paths.get(storageFolder.absolutePath, "photos/_students/$studentId")

        private fun paymentFilesPath(paymentId: Long, fieldId: String) = Paths.get(storageFolder.absolutePath, "photos/_payments/$paymentId/$fieldId")

        private fun homeworkFilesPath(homeworkId: Long) = Paths.get(storageFolder.absolutePath, "_homework/$homeworkId")

        private fun lessonFilesPath(lessonId: String) = Paths.get(storageFolder.absolutePath, "photos/_lesson/$lessonId")

        private fun studentCardFilesPath(cardId: Long) = Paths.get(storageFolder.absolutePath, "photos/_studentCard/$cardId")
    }
}

class StorageProviderImpl(val rootPath: Path) : StorageProvider {
    private val visibleFile: (Path) -> Boolean = { Files.isRegularFile(it) && !Files.isHidden(it) }

    override fun listNames(): List<String> {
        return if (Files.exists(rootPath)) {
            Files.list(rootPath).filter(visibleFile).map { it.toFile().name }.sorted().collect(Collectors.toList<String>())
        } else {
            emptyList()
        }
    }

    override fun list(): List<File> {
        return if (Files.exists(rootPath)) {
            Files.list(rootPath).filter(visibleFile).map(Path::toFile).sorted().collect(Collectors.toList<File>())
        } else {
            emptyList()
        }
    }

    override fun filesCount(): Int {
        return if (Files.exists(rootPath)) {
            Files.list(rootPath).filter(visibleFile).count().toInt()
        } else {
            0
        }
    }

    override fun download(fileName: String, output: OutputStream) {
        val filePath = filePath(fileName)
        if (Files.isRegularFile(filePath)) {
            Files.copy(filePath, output)
        }
    }

    override fun exists(fileName: String): Boolean {
        val filePath = filePath(fileName)
        return filePathExists(filePath)
    }

    override fun size(fileName: String): Long {
        val filePath = filePath(fileName)
        return if (filePathExists(filePath)) Files.size(filePath) else -1L
    }

    override fun upload(fileName: String, input: InputStream) {
        rootPath.toFile().mkdirs()
        Files.copy(input, filePath(fileName), StandardCopyOption.REPLACE_EXISTING)
    }

    override fun delete(fileName: String) {
        val filePath = filePath(fileName)
        if (Files.isRegularFile(filePath)) {
            Files.deleteIfExists(filePath)
        }
    }

    override fun deleteRoot() {
        rootPath.toFile().deleteRecursively()
    }

    private fun filePath(fileName: String): Path = rootPath.resolve(fileName)

    private fun filePathExists(filePath: Path): Boolean = Files.exists(filePath) && Files.isRegularFile(filePath)
}