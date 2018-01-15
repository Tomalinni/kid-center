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
import com.joins.kidcenter.domain.Card
import com.joins.kidcenter.dto.CardSearchRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.CardService
import com.joins.kidcenter.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/cards")
class CardController @Autowired constructor(
        val service: CardService,
        val objectMapper: ObjectMapper) {


    @RequestMapping("/", method = arrayOf(RequestMethod.GET))
    fun findAll(response: HttpServletResponse, @RequestParam parameters: Map<String, String>) {
        SecurityUtil.checkPermission(Permission.cardsRead)
        val searchResult = service.findAll(CardSearchRequest.Factory.fromMap(parameters))
        response.sendObject(objectMapper, searchResult)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.GET))
    fun findOne(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.cardsRead)
        val card: Card? = service.findOne(id)
        if (card != null) {
            response.sendObject(objectMapper, card)
        } else {
            response.sendStatus(HttpStatus.NOT_FOUND)
        }
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(response: HttpServletResponse, @RequestBody card: Card) {
        SecurityUtil.checkPermission(Permission.cardsModify)
        response.sendSaveResult(objectMapper, service.save(card))

    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.PUT))
    fun modify(response: HttpServletResponse, @PathVariable id: Long, @RequestBody card: Card) {
        SecurityUtil.checkPermission(Permission.cardsModify)
        card.id = id
        val modifiedCard = service.save(card)
        response.sendSaveResult(objectMapper, modifiedCard)
    }

    @RequestMapping(path = arrayOf("/{id}"), method = arrayOf(RequestMethod.DELETE))
    fun delete(response: HttpServletResponse, @PathVariable id: Long) {
        SecurityUtil.checkPermission(Permission.cardsModify)
        response.sendDeleteResult(objectMapper, service.delete(id))
    }

}
