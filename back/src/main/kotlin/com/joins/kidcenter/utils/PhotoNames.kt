package com.joins.kidcenter.utils

import org.apache.commons.lang3.StringUtils

object PhotoNames {
    val photoNamePrefix = "photo"
    val firstPhotoIndex = 1

    fun unusedPhotoNames(existingPhotoNames: Collection<String>, newPhotoNames: List<String>): List<String> {
        val existingIndexes = existingPhotoNames.map { getPhotoIndex(it) }
        var curPhotoIndex = firstPhotoIndex
        return newPhotoNames.map {
            if (!existingIndexes.isEmpty()) {
                while (existingIndexes.contains(curPhotoIndex)) {
                    curPhotoIndex++
                }
            }
            newPhotoName(curPhotoIndex++, getFileNameExtension(it))
        }
    }

    private fun getPhotoIndex(photoName: String): Int {
        if (!StringUtils.isBlank(photoName)) {
            val hasPrefix = photoName.startsWith(photoNamePrefix)
            val extensionDotIndex = photoName.lastIndexOf('.')
            if (hasPrefix && extensionDotIndex > -1) {
                return photoName.substring(photoNamePrefix.length, extensionDotIndex).toInt(-1)
            }
        }
        return -1
    }

    fun getFileNameExtension(fileName: String): String {
        val extensionDotIndex = fileName.lastIndexOf('.')
        if (extensionDotIndex > -1) {
            return fileName.substring(extensionDotIndex + 1, fileName.length)
        }
        return ""
    }

    private fun newPhotoName(photoIndex: Int, extension: String): String {
        val extensionPart = if (StringUtils.isBlank(extension)) "" else ".$extension"
        return "$photoNamePrefix$photoIndex$extensionPart"
    }

}
