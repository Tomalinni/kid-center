package com.joins.kidcenter.service

import com.joins.kidcenter.domain.AppPermission
import com.joins.kidcenter.domain.AppRole
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.AppRoleRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import com.joins.kidcenter.utils.ValidatorsUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface RoleService : SaveService<RoleDto, SaveResult<RoleDto>> {
    fun findAll(searchRequest: TextSearchRequest): SearchResult<RoleDto>
    fun findOne(id: String): RoleDto?
    fun delete(id: String): DeleteResult<String>
}

@Service
@Transactional
open class RoleServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: AppRoleRepository,
        val authService: AuthService
) : EntityService(em, queryBuilder), RoleService {

    @Transactional(readOnly = true)
    override fun findAll(searchRequest: TextSearchRequest): SearchResult<RoleDto> {

        val result = findByRequest<AppRole>(searchRequest, AppRole::class.java, listOf("id"))
        return SearchResult<RoleDto>(result.results.map{RoleDto.fromDomainObject(it)}, result.total)
    }

    @Transactional(readOnly = true)
    override fun findOne(id: String): RoleDto? {
        val appRole = repository.findOne(id)
        return if (appRole == null) null else RoleDto.fromDomainObject(appRole)
    }

    override fun save(entity: RoleDto): SaveResult<RoleDto> {

        val result: SaveResult<RoleDto> = ValidatorsUtil.validateRole(entity)
        if (result.hasErrors()) {
            return result
        }
        val role: AppRole
        if (entity.id.isNullOrBlank()) {
            if (repository.exists(entity.newId)) {
                result.addValidationMessage(EntityId.roles, "newId", "common.users.validation.user.already.exists")
                return result
            }
            entity.id = entity.newId!!
            role = AppRole().apply {
                id = entity.id
            }
        } else {
            role = repository.findOne(entity.id)
        }
        role.apply {
            permissions = entity.permissions.map({ AppPermission().apply { id = it } }).toMutableSet()
        }
        repository.save(role)

        authService.refreshUserTokens(repository.findUserIdsByRoleId(role.id!!))
        return SaveResult(entity)
    }

    override fun delete(id: String): DeleteResult<String> {
        repository.delete(id)
        return DeleteResult(id)
    }
}