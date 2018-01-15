package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.domain.KinderGarden
import com.joins.kidcenter.dto.EntityRef
import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.security.model.Permission
import com.joins.kidcenter.service.KinderGardenService
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.sendObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/kinderGardens")
class KinderGardenController @Autowired constructor(
        val service: KinderGardenService,
        val objectMapper: ObjectMapper) {

    @RequestMapping("/", method = arrayOf(RequestMethod.GET))
    fun findAll(response: HttpServletResponse, @Param("text") text: String) {
        SecurityUtil.checkAnyPermission(Permission.studentsRead, Permission.hasChildren)
        val searchResult = service.findAll(TextSearchRequest.Factory.fromText(text))
        response.sendObject(objectMapper.writer(), searchResult)
    }

    @RequestMapping(path = arrayOf("/"), method = arrayOf(RequestMethod.POST))
    fun create(@RequestBody kinderGarden: KinderGarden): EntityRef<Long> {
        SecurityUtil.checkPermission(Permission.studentsModify)
        return EntityRef(service.save(kinderGarden).id!!)
    }
}
