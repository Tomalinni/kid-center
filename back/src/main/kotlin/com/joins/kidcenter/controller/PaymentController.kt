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
import com.joins.kidcenter.AppMimeTypes
import com.joins.kidcenter.domain.Payment
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.PaymentService
import com.joins.kidcenter.service.export.PaymentExportService
import com.joins.kidcenter.service.storage.FileStorageServiceImpl
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/payments")
class PaymentController @Autowired constructor(
        val service: PaymentService,
        val exportService: PaymentExportService,
        val storageService: FileStorageServiceImpl,
        val objectMapper: ObjectMapper) {


    @RequestMapping("/", method = arrayOf(RequestMethod.GET))
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        val searchResult = service.findAll(PaymentSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, searchResult)
    }

    @RequestMapping("/stat", method = arrayOf(RequestMethod.GET))
    fun findStat(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        val result: LineChartResult = service.findStat(PaymentSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, result)
    }

    @RequestMapping("/balance", method = arrayOf(RequestMethod.GET))
    fun findBalance(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        val result: BalanceResult = service.findBalance(PaymentBalanceRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, result)
    }

    @RequestMapping("/export/payments", method = arrayOf(RequestMethod.GET))
    fun exportPayments(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        response.sendFile("export.xlsx", AppMimeTypes.xlsx,
                { out -> exportService.export(PaymentSearchRequest.Factory.fromMap(parameters), out) })
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        val payment: PaymentDto? = service.findOne(id)
        response.sendObject(objectMapper, payment)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody payment: Payment) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendSaveResult(objectMapper, service.save(payment))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody payment: Payment) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        response.sendSaveResult(objectMapper, service.save(payment.apply { this.id = id }))
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        val result = service.delete(id)

        if (result.success()) {
            PaymentPhotoFields.values().forEach {
                storageService.providers().payment(id, it.toString()).deleteRoot() //todo make storage folders and subfolders
            }
        }
        response.sendDeleteResult(objectMapper, result)
    }

    private fun fillStorageData(payment: PaymentDto) {
        payment.receiptPhotos = storageService.providers().payment(payment.id, PaymentPhotoFields.receiptPhotos.toString()).listNames()
        payment.productPhotos = storageService.providers().payment(payment.id, PaymentPhotoFields.productPhotos.toString()).listNames()
    }

    @RequestMapping(path = arrayOf("/{paymentId}/photos/{fieldId}"), method = arrayOf(RequestMethod.GET))
    fun listPhotos(response: HttpServletResponse, @PathVariable paymentId: Long, @PathVariable fieldId: String) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        val payment = service.findOne(paymentId)
        if (payment != null) {
            val names = storageService.providers().payment(paymentId, fieldId).listNames()
            response.sendObject(objectMapper, FileNames(names))
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }


    @RequestMapping(path = arrayOf("/{paymentId}/photos/{fieldId}"), method = arrayOf(RequestMethod.POST))
    fun uploadPhoto(response: HttpServletResponse, @PathVariable paymentId: Long, @PathVariable fieldId: String, @RequestParam(name = "photo") photos: Collection<MultipartFile>) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        val payment = service.findOne(paymentId)
        if (payment != null) {
            val storageProvider = storageService.providers().payment(paymentId, fieldId)
            val existingPhotoNames = storageProvider.listNames()
            val newPhotoNames = PhotoNames.unusedPhotoNames(existingPhotoNames, photos.map { it.originalFilename })
            photos.forEachIndexed { i, file -> storageProvider.upload(newPhotoNames[i], file.inputStream) }
            service.updatePhotosCount(paymentId, fieldId, existingPhotoNames.size + newPhotoNames.size)
            response.sendObject(objectMapper, FileNames(newPhotoNames))
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{paymentId}/photos/{fieldId}/{photoName:.+}"), method = arrayOf(RequestMethod.GET))
    fun downloadPhoto(response: HttpServletResponse, @PathVariable paymentId: Long, @PathVariable fieldId: String, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.paymentsRead)
        val payment = service.findOne(paymentId)
        val storageProvider = storageService.providers().payment(paymentId, fieldId)
        if (payment != null && storageProvider.exists(photoName)) {
            storageProvider.download(photoName, response.outputStream)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/{paymentId}/photos/{fieldId}/{photoName:.+}"), method = arrayOf(RequestMethod.DELETE))
    fun deletePhoto(response: HttpServletResponse, @PathVariable paymentId: Long, @PathVariable fieldId: String, @PathVariable photoName: String) {
        SecurityUtil.checkPermission(Permission.paymentsModify)
        val payment = service.findOne(paymentId)
        if (payment != null) {
            val storageProvider = storageService.providers().payment(paymentId, fieldId)
            storageProvider.delete(photoName)
            service.updatePhotosCount(paymentId, fieldId, storageProvider.filesCount())
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }
}

enum class PaymentPhotoFields {
    productPhotos, receiptPhotos
}
