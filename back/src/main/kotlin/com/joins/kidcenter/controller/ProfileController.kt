package com.joins.kidcenter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.joins.kidcenter.dto.ProfileDto
import com.joins.kidcenter.service.ProfileService
import com.joins.kidcenter.utils.sendSaveResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("data/profile")
class ProfileController @Autowired constructor(
        val service: ProfileService,
        val objectMapper: ObjectMapper) {

    @RequestMapping("/save", method = arrayOf(RequestMethod.POST))
    fun save(response: HttpServletResponse, @RequestBody profileDto: ProfileDto) {
        response.sendSaveResult(objectMapper, service.save(profileDto))
    }
}