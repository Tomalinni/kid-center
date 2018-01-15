package com.joins.kidcenter.service

import com.joins.kidcenter.dto.ProfileDto
import com.joins.kidcenter.dto.SaveResult
import com.joins.kidcenter.repository.AppUserRepository
import com.joins.kidcenter.utils.PasswordEncryptor
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.ValidatorsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ProfileService : SaveService<ProfileDto, SaveResult<ProfileDto>> {}

@Service
@Transactional
open class ProfileServiceImpl @Autowired constructor(
        val userRepository: AppUserRepository,
        val encryptor: PasswordEncryptor
) : ProfileService {
    override fun save(entity: ProfileDto): SaveResult<ProfileDto> {
        val user = userRepository.findOne(SecurityUtil.subject())
        val oldPass = entity.oldPass
        entity.oldPass = if (oldPass.isNullOrBlank()) oldPass else encryptor.encrypt(oldPass!!)
        val validation = ValidatorsUtil.validateProfile(entity, user)
        if (validation.hasErrors()) {
            return validation;
        }

        val newPass = entity.newPass
        if (!newPass.isNullOrBlank()) {
            user.pass = encryptor.encrypt(newPass!!)
        }
        userRepository.save(user);
        return SaveResult(ProfileDto(user.id!!))
    }
}