/*
 * (C) Copyright ${YEAR} Legohuman (https://github.com/Legohuman).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.domain.StudentCard
import com.joins.kidcenter.dto.FileNames
import com.joins.kidcenter.dto.StudentCardPaymentRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.StudentCardService
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/studentCards")
class StudentCardController @Autowired constructor(
        val service: StudentCardService,
        val objectMapper: ObjectMapper,
        val storageService: FileStorageServiceImpl) {

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentCardsRead)
        val card: StudentCard? = service.findOne(id)
        response.sendObject(objectMapper, card, StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody createRequest: StudentCardPaymentRequest) {
        SecurityUtil.checkPermission(Permission.studentCardsModify)
        return response.sendSaveResult(objectMapper, service.create(createRequest), StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody card: StudentCard) {
        SecurityUtil.checkPermission(Permission.studentCardsModify)
        card.id = id
        response.sendSaveResult(objectMapper, service.save(card), StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.studentCardsModify)
        val result = service.delete(id)
        if (result.success()) {
            storageService.providers().studentCard(id).deleteRoot()
        }
        response.sendDeleteResult(objectMapper, result)
    }

    @RequestMapping(path = arrayOf("/{cardId}/payments"), method = arrayOf(RequestMethod.PUT))
    fun addPayment(response: HttpServletResponse, @PathVariable cardId: Long, @RequestBody paymentRequest: StudentCardPaymentRequest) {
        SecurityUtil.checkPermission(Permission.studentCardsModify)
        paymentRequest.card = StudentCard().apply {
            id = cardId
        }
        response.sendSaveResult(objectMapper, service.addPayment(paymentRequest), StudentController.referenceFilterProvider)
    }

    @RequestMapping(path = arrayOf("/{cardId}/photos"), method = arrayOf(RequestMethod.POST))
    fun uploadStudentCardPhoto(response: HttpServletResponse, @PathVariable cardId: Long, @RequestParam(name = "photo") photos: Collection<MultipartFile>) {
        SecurityUtil.checkPermission(Permission.studentCardsModify)
        val lesson = service.findOne(cardId)
        if (lesson != null) {
            val storageProvider = storageService.providers().studentCard(cardId)
            val newPhotoNames = PhotoNames.unusedPhotoNames(storageProvider.listNames(), photos.map { it.originalFilename })
            photos.forEachIndexed { i, file -> storageProvider.upload(newPhotoNames[i], file.inputStream) }
            response.sendObject(objectMapper, FileNames(newPhotoNames))
        }
    }

    @RequestMapping(path = arrayOf("/{cardId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.GET))
    fun downloadStudentCardPhoto(response: HttpServletResponse, @PathVariable cardId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentCardsRead)
        val card = service.findOne(cardId)
        val storageProvider = storageService.providers().studentCard(cardId)
        if (card != null && storageProvider.exists(photoName)) {
            storageProvider.download(photoName, response.outputStream)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{cardId}/photos/{photoName:.+}"), method = arrayOf(RequestMethod.DELETE))
    fun deleteStudentCardPhoto(response: HttpServletResponse, @PathVariable cardId: Long, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.studentCardsModify)
        val card = service.findOne(cardId)
        if (card != null) {
            val storageProvider = storageService.providers().studentCard(cardId)
            storageProvider.delete(photoName)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{cardId}/photos/"), method = arrayOf(RequestMethod.GET))
    fun findPhotos(response: HttpServletResponse, @PathVariable cardId: Long) {
        val photos = storageService.providers().studentCard(cardId).listNames()
        response.sendObject(objectMapper, FileNames(photos))
    }
}
