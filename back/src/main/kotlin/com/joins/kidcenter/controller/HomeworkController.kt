package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.dto.FileNames
import com.joins.kidcenter.dto.HomeworkDto
import com.joins.kidcenter.dto.HomeworkSearchRequest
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.HomeworkService
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("data/homework")
class HomeworkController @Autowired constructor(
        val objectMapper: ObjectMapper,
        val service: HomeworkService,
        val storageService: FileStorageServiceImpl) {

    @RequestMapping("/", method = arrayOf(RequestMethod.GET))
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.homeworkRead)
        val homework = service.findAll(HomeworkSearchRequest.Factory.fromMap(parameters))
        homework.results.forEach { hw -> fillStorageData(hw) }
        response.sendObject(objectMapper, homework)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.homeworkRead)
        val homework = service.findOne(id)
        fillStorageData(homework)
        response.sendObject(objectMapper, homework)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody homework: HomeworkDto) {
        SecurityUtil.checkPermission(Permission.homeworkModify)
        val result: SaveResult<HomeworkDto> = service.save(homework)
        response.sendSaveResult(objectMapper, result)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody homework: HomeworkDto) {
        SecurityUtil.checkPermission(Permission.homeworkModify)
        val result: SaveResult<HomeworkDto> = service.save(homework)
        response.sendSaveResult(objectMapper, result)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.homeworkModify)
        val result = service.delete(id)
        if (result.success()) {
            deleteAttachments(id)
        }
        response.sendDeleteResult(objectMapper, result)
    }

    @RequestMapping(path = arrayOf("/{homeworkId}/attachments/{name:.+}"), method = arrayOf(RequestMethod.GET))
    fun downloadHomework(response: HttpServletResponse, @PathVariable homeworkId: Long, @PathVariable name: String) {
        SecurityUtil.checkPermission(Permission.homeworkRead)
        val homework = service.findOne(homeworkId)
        val storageProvider = storageService.providers().homework(homeworkId)
        if (homework != null && storageProvider.exists(name)) {
            response.contentType = "application/x-file-download"
            storageProvider.download(name, response.outputStream)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{homeworkId}/attachments"), method = arrayOf(RequestMethod.POST))
    fun uploadHomework(response: HttpServletResponse, @PathVariable homeworkId: Long, @RequestParam(name = "attachment") attachments: Collection<MultipartFile>) {
        SecurityUtil.checkPermission(Permission.homeworkModify)
        val homework = service.findOne(homeworkId)
        if (homework != null) {
            val storageProvider = storageService.providers().homework(homeworkId)
            val newAttachmentNames = attachments.map { it.originalFilename }
            attachments.forEachIndexed { i, file -> storageProvider.upload(newAttachmentNames[i], file.inputStream) }
            response.sendObject(objectMapper, FileNames(newAttachmentNames))
        }
    }

    @RequestMapping(path = arrayOf("/{homeworkId}/attachments/{name:.+}"), method = arrayOf(RequestMethod.DELETE))
    fun deleteHomeworkAttachment(response: HttpServletResponse, @PathVariable homeworkId: Long, @PathVariable name: String) {
        SecurityUtil.checkPermission(Permission.homeworkModify)
        val homework = service.findOne(homeworkId)
        if (homework != null) {
            val storageProvider = storageService.providers().homework(homeworkId)
            storageProvider.delete(name)
            service.save(homework)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    fun fillStorageData(homework: HomeworkDto?) {
        if (homework == null) {
            return
        }
        val storage = storageService.providers().homework(homework.id)
        homework.files = storage.listNames()
    }

    fun deleteAttachments(homeworkId: Long) {
        val storage = storageService.providers().homework(homeworkId)
        storage.listNames().forEach { name -> storage.delete(name) }
    }
}