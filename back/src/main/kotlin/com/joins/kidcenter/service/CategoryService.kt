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

package com.joins.kidcenter.service

import com.joins.kidcenter.domain.Category
import com.joins.kidcenter.dto.*
import com.joins.kidcenter.repository.CategoryRepository
import com.joins.kidcenter.service.persistence.EntityListQueryBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

interface CategoryService : FindOneService<CategoryDto>, SaveService<CategoryDto, SaveResult<CategoryDto>>, DeleteOneService {
    fun findAll(): Map<Long, CategoryDto>
}

@Service
@Transactional
open class CategoryServiceImpl @Autowired constructor(
        em: EntityManager,
        queryBuilder: EntityListQueryBuilder,
        val repository: CategoryRepository
) : EntityService(em, queryBuilder), CategoryService {

    private val maxCategoryLevel = 4
    private val categoryLevels: Map<Int, String> = mapOf(
            Pair(0, "category"),
            Pair(1, "category2"),
            Pair(2, "category3"),
            Pair(3, "category4"),
            Pair(4, "category5")
    )

    @Transactional(readOnly = true)
    override fun findOne(id: Long): CategoryDto? = CategoryDto.fromDomainObject(repository.findOne(id))

    @Transactional(readOnly = true)
    override fun findAll(): Map<Long, CategoryDto> {
        val categories: Map<Long, CategoryDto> = repository.findOrderById().map {
            val dto = CategoryDto.fromDomainObject(it)
            Pair(dto.id, dto)
        }.toMap()
        categories.values.forEach { dto ->
            if (dto.parent != null) {
                val parentCategory = categories[dto.parent!!.id]
                if (parentCategory != null) {
                    parentCategory.children.add(EntityRef(dto.id))
                }
            }
        }
        return categories
    }

    override fun save(entity: CategoryDto): SaveResult<CategoryDto> {
        val category = Category().apply {
            id = entity.id
            name = entity.name
            parent = if (entity.parent != null) repository.findOne(entity.parent!!.id) else null
            hasTargetMonth = entity.hasTargetMonth
            level = if (parent != null) parent!!.level + 1 else 0
        }

        val level = category.level

        if (level <= maxCategoryLevel) {

            if (!category.isNew()) { //can have children
                val oldCategory = repository.findOne(category.id!!)
                if (oldCategory != null) {
                    val idsAtLevels: MutableMap<Int, List<Long>> = mutableMapOf(Pair(level, listOf(category.id!!)))
                    for (curLevel in level..maxCategoryLevel - 1) {
                        val ids = repository.findIdsByParentIds(idsAtLevels[curLevel]!!)
                        if (ids.isEmpty()) {
                            break
                        }
                        idsAtLevels.put(curLevel + 1, ids)
                    }
                    val maxLevelCategoryIds = idsAtLevels[maxCategoryLevel]
                    if (maxLevelCategoryIds != null && !repository.findIdsByParentIds(maxLevelCategoryIds).isEmpty()) {
                        return SaveResult(EntityId.categories, "common.categories.validation.subcategories.max.level")
                    }

                    idsAtLevels.forEach {
                        repository.updateLevelByIds(it.value, it.key)
                    }

                    val oldCategoryLevel = oldCategory.level
                    val savedCategory = repository.save(category)
                    updateRelatedPayments(savedCategory, oldCategoryLevel)
                    return SaveResult(CategoryDto.fromDomainObject(savedCategory))
                }
            }
            return SaveResult(CategoryDto.fromDomainObject(repository.save(category)))
        } else {
            return SaveResult(EntityId.categories, "common.categories.validation.max.level")
        }
    }

    private fun updateRelatedPayments(category: Category, oldCategoryLevel: Int) {
        val level = category.level
        val oldPaymentCategoryCol = categoryLevels[oldCategoryLevel]!!
        val categoryColVals = mutableMapOf<String, Long>()
        var curCategory = category
        for (curLevel in level downTo 0) {
            categoryColVals[categoryLevels[curLevel]!!] = curCategory.id!!
            //protect from unsynchronisation between levels and categories hierarchy, put the same category in lower categories for this case
            curCategory = if (curCategory.parent == null) curCategory else curCategory.parent!!
        }
        val replacedCategoryCols = mutableMapOf<String, String?>()
        val levelDiff = level - oldCategoryLevel
        for (curLevel in (level + 1)..maxCategoryLevel) {
            replacedCategoryCols[categoryLevels[curLevel]!!] = categoryLevels[curLevel - levelDiff]
        }
        val colAssignments = categoryColVals.map { "${it.key} = ${it.value}" }.plus(replacedCategoryCols.map { "${it.key} = ${it.value}" })
        em.createQuery("update Payment set ${colAssignments.joinToString()} where $oldPaymentCategoryCol=:category").setParameter("category", category).executeUpdate()
    }

    override fun delete(id: Long): DeleteResult<Long> {
        repository.delete(id)
        return DeleteResult(id)
    }
}