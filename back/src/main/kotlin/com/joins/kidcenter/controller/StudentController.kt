package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.joins.kidcenter.domain.Student
import com.joins.kidcenter.domain.StudentCardMetadata
import com.joins.kidcenter.domain.StudentMetadata
import com.joins.kidcenter.dto.FileNames
import com.joins.kidcenter.dto.StudentRelativesDto
import com.joins.kidcenter.dto.StudentSearchRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.StudentRelativeService
import com.joins.kidcenter.service.StudentService
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/students")
class StudentController @Autowired constructor(
        val service: StudentService,
        val relativeService: StudentRelativeService,
        val objectMapper: ObjectMapper,
        val storageService: FileStorageServiceImpl) {

    companion object {
        val listItemFilterProvider = SimpleFilterProvider()
                .addFilter(StudentMetadata.Filter.id, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.listItemIncludedFields))
                .addFilter(StudentCardMetadata.Filter.id, SimpleBeanPropertyFilter.serializeAll())
                .addFilter(StudentMetadata.Filter.referenceAndName, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.referenceAndNameIncludedFields))
                .addFilter(StudentMetadata.Filter.info, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.infoIncludedFields))
        val listItemWithCardsFilterProvider = SimpleFilterProvider()
                .addFilter(StudentMetadata.Filter.id, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.listItemWithCardsIncludedFields))
                //remove back referece to student from student card to prevent infinite serialization cycle
                .addFilter(StudentCardMetadata.Filter.id, SimpleBeanPropertyFilter.serializeAllExcept(*StudentCardMetadata.Filter.studentCol))
                .addFilter(StudentMetadata.Filter.referenceAndName, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.referenceAndNameIncludedFields))
                .addFilter(StudentMetadata.Filter.info, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.infoIncludedFields))
        val editFormFilterProvider = SimpleFilterProvider()
                .addFilter(StudentMetadata.Filter.id, SimpleBeanPropertyFilter.serializeAllExcept(*StudentMetadata.Filter.formExcludedFields))
                .addFilter(StudentCardMetadata.Filter.id, SimpleBeanPropertyFilter.serializeAll())
                .addFilter(StudentMetadata.Filter.referenceAndName, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.referenceAndNameIncludedFields))
                .addFilter(StudentMetadata.Filter.info, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.infoIncludedFields))
        val referenceFilterProvider = SimpleFilterProvider()
                .addFilter(StudentMetadata.Filter.id, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.referenceIncludedFields))
                .addFilter(StudentCardMetadata.Filter.id, SimpleBeanPropertyFilter.serializeAll())
                .addFilter(StudentMetadata.Filter.referenceAndName, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.referenceAndNameIncludedFields))
                .addFilter(StudentMetadata.Filter.info, SimpleBeanPropertyFilter.filterOutAllExcept(*StudentMetadata.Filter.infoIncludedFields))
    }

    @RequestMapping("/")
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.studentsRead)
        val searchResult = service.findAll(StudentSearchRequest.Factory.fromMap(parameters))
        response.sendObject(writerWithFilterProvider(selectFieldFilterProvider(parameters["columnsGroup"])), searchResult)
    }

    private fun selectFieldFilterProvider(columnsGroup: String?) =
            when (columnsGroup) {
                "withCards" -> listItemWithCardsFilterProvider
                else -> listItemFilterProvider
            }


    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentsRead)
        val student: Student? = service.findOne(id)
        if (student != null) {
            fillStorageData(student)
            response.sendObject(writerWithFilterProvider(editFormFilterProvider), student)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody student: Student) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        return response.sendSaveResult(objectMapper, service.save(student), editFormFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody student: Student) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        student.id = id
        deleteRemovedRelativesFiles(id, student)
        val modifiedStudent = service.save(student)
        if (!modifiedStudent.hasErrors() && modifiedStudent.obj != null) {
            fillStorageData(modifiedStudent.obj)
        }
        response.sendSaveResult(objectMapper, modifiedStudent, editFormFilterProvider)
    }

    @RequestMapping(path = arrayOf("/student/relatives/notifications"), method = arrayOf(RequestMethod.POST))
    fun saveNotifications(response: HttpServletResponse, @RequestBody request: StudentRelativesDto) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.saveRelativesNotifications(request)
        if (student != null) {
            response.sendSaveResult(objectMapper, service.save(student), editFormFilterProvider)
        }
    }

    private fun deleteRemovedRelativesFiles(id: Long, student: Student) {
        val oldStudent = service.findOne(id)
        if (oldStudent != null) {
            val processedRelativeIds = oldStudent.relatives.map { it -> it.id!! }.toMutableList()
            val newRelativeIds = student.relatives.map { it -> it.id }.toMutableList()
            processedRelativeIds.removeAll(newRelativeIds) //find removed ids
            processedRelativeIds.forEach { relativeId ->
                storageService.providers().studentRelative(id, relativeId).deleteRoot()
            }
        }
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val result = service.delete(id)
        if (result.success()) {
            storageService.providers().student(id).deleteRoot()
        }
        response.sendDeleteResult(objectMapper, result)
    }

    @RequestMapping(path = arrayOf("/{studentId}/photos"), method = arrayOf(RequestMethod.POST))
    fun uploadStudentPhoto(response: HttpServletResponse, @PathVariable studentId: Long, @RequestParam(name = "photo") photos: Collection<MultipartFile>) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.findOne(studentId)
        if (student != null) {
            val storageProvider = storageService.providers().student(studentId)
            val newPhotoNames = PhotoNames.unusedPhotoNames(storageProvider.listNames(), photos.map { it.originalFilename })
            photos.forEachIndexed { i, file -> storageProvider.upload(newPhotoNames[i], file.inputStream) }
            response.sendObject(objectMapper, FileNames(newPhotoNames))
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.GET))
    fun downloadStudentPhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentsRead)
        val student = service.findOne(studentId)
        val storageProvider = storageService.providers().student(studentId)
        if (student != null && storageProvider.exists(photoName)) {
            storageProvider.download(photoName, response.outputStream)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.DELETE))
    fun deleteStudentPhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.findOne(studentId)
        if (student != null) {
            val storageProvider = storageService.providers().student(studentId)
            storageProvider.delete(photoName)
            val photoNames = storageProvider.listNames()
            student.primaryPhotoName = if (photoNames.isNotEmpty()) photoNames[0] else null
            service.save(student)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/photos/{photoName:.+}/primary"), method = arrayOf(RequestMethod.PUT))
    fun setStudentPrimaryPhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.findOne(studentId)
        val storageProvider = storageService.providers().student(studentId)
        if (student != null && storageProvider.exists(photoName)) {
            student.primaryPhotoName = photoName
            service.save(student)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/relatives/{relativeId}/photos"), method = arrayOf(RequestMethod.POST))
    fun uploadStudentRelativePhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable relativeId: Long, @RequestParam(name = "photo") photos: Collection<MultipartFile>) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.findOne(studentId)
        val studentRelative = relativeService.findOne(relativeId)

        if (student != null && studentRelative != null) {
            val storageProvider = storageService.providers().studentRelative(studentId, relativeId)
            val newPhotoNames = PhotoNames.unusedPhotoNames(storageProvider.listNames(), photos.map { it.originalFilename })
            photos.forEachIndexed { i, file -> storageProvider.upload(newPhotoNames[i], file.inputStream) }
            response.sendObject(objectMapper, FileNames(newPhotoNames))
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/relatives/{relativeId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.GET))
    fun downloadStudentRelativePhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable relativeId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentsRead)
        val student = service.findOne(studentId)
        val studentRelative = relativeService.findOne(relativeId)
        val storageProvider = storageService.providers().studentRelative(studentId, relativeId)

        if (student != null && studentRelative != null && storageProvider.exists(photoName)) {
            storageProvider.download(photoName, response.outputStream)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/relatives/{relativeId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.DELETE))
    fun deleteStudentRelativePhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable relativeId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.findOne(studentId)
        val studentRelative = relativeService.findOne(relativeId)

        if (student != null && studentRelative != null) {
            val storageProvider = storageService.providers().studentRelative(studentId, relativeId)
            storageProvider.delete(photoName)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{studentId}/relatives/{relativeId}/photos/{photoName:.+}/primary"), method = arrayOf(RequestMethod.PUT))
    fun setStudentRelativePrimaryPhoto(response: HttpServletResponse, @PathVariable studentId: Long, @PathVariable relativeId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentsModify)
        val student = service.findOne(studentId)
        val studentRelative = relativeService.findOne(relativeId)

        val storageProvider = storageService.providers().studentRelative(studentId, relativeId)
        if (student != null && studentRelative != null && storageProvider.exists(photoName)) {
            studentRelative.primaryPhotoName = photoName
            relativeService.save(studentRelative)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    private fun fillStorageData(student: Student) {
        student.photos = storageService.providers().student(student.id!!).listNames()
        student.relatives.forEach { relative ->
            relative.photos = storageService.providers().studentRelative(student.id!!, relative.id!!).listNames()
        }
    }

    private fun writerWithFilterProvider(provider: FilterProvider?) = objectMapper.writer().with(provider)

    @RequestMapping(path = arrayOf("/total/lessons/"), method = arrayOf(RequestMethod.PUT))
    fun updateLessonTotals(response: HttpServletResponse) {
        SecurityUtil.checkAllPermissions(Permission.studentsModify, Permission.studentCardsModify)
        service.updateLessonTotals()
        response.sendStatus(HttpStatus.OK)
    }
}
