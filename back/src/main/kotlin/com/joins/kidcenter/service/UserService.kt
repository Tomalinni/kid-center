package com.joins.kidcenter.service

import com.joins.kidcenter.domain.AppRole
import com.joins.kidcenter.domain.AppUser
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.AppUserRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.SecurityUtil
import com.joins.kidcenter.utils.ValidatorsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface UserService : SaveService<UserDto, SaveResult<UserDto>> {
    fun findAll(searchRequest: TextSearchRequest): SearchResult<UserDto>
    fun findOne(id: String): UserDto?
    fun delete(id: String): DeleteResult<String>
}

@Service
@Transactional
open class UserServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: AppUserRepository,
        val authService: AuthService
) : EntityService(em, queryBuilder), UserService {

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: TextSearchRequest): SearchResult<UserDto> {
        val users: SearchResult<AppUser> = findByRequest(searchRequest, AppUser::class.java, listOf("id", "name"))
        return SearchResult(users.results.map { UserDto.fromDomainObject(it) }, users.total)
    }

    @Transactional(readOnly = true)
    override fun findOne(id: String): UserDto? {
        val appUser = repository.findOne(id)
        return if (appUser == null) null else UserDto.fromDomainObject(appUser)
    }

    override fun save(entity: UserDto): SaveResult<UserDto> {

        val result: SaveResult<UserDto> = ValidatorsUtil.validateUser(entity)
        if (result.hasErrors()) {
            return result
        }
        val user: AppUser
        if (entity.id.isNullOrBlank()) {
            if (repository.exists(entity.newId)) {
                result.addValidationMessage(EntityId.users, "newId", "common.users.validation.user.already.exists")
                return result
            }
            entity.id = entity.newId!!
            user = AppUser().apply {
                id = entity.id
            }
        } else {
            user = repository.findOne(entity.id)
        }
        user.apply {
            name = entity.name
            pass = if (entity.passChanged) entity.pass else pass
            roles = entity.roles.map { AppRole().apply { id = it.id } }.toMutableSet()
        }
        repository.save(user)

        if (entity.passChanged && SecurityUtil.subject() != user.id!!) {
            authService.invalidateUserTokens(listOf(user.id!!))
        } else {
            authService.refreshUserTokens(listOf(user.id!!))
        }
        return SaveResult(entity)
    }

    override fun delete(id: String): DeleteResult<String> {
        repository.delete(id)
        em.flush()
        authService.invalidateUserTokens(listOf(id))
        return DeleteResult(id)
    }

}
