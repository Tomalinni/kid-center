package com.joins.kidcenter.service.persistence

import com.joins.kidcenter.dto.TextSearchRequest
import com.joins.kidcenter.utils.PredicateUtil.fieldsContainClause
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.persistence.EntityManagerFactory
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

interface EntityListQueryBuilder {
    fun queries(data: QuerySourceData): CriteriaQueries
}

data class CriteriaQueries(val list: CriteriaQuery<Any>,
                           val count: CriteriaQuery<Any>)

data class QuerySourceData(val root: Class<out Any>,
                           val containsClauseFields: Collection<String>,
                           val query: TextSearchRequest)

@Service
class EntityListQueryBuilderImpl
@Autowired constructor(emf: EntityManagerFactory) : EntityListQueryBuilder {

    val cb: CriteriaBuilder = emf.criteriaBuilder

    override fun queries(data: QuerySourceData): CriteriaQueries =
            CriteriaQueries(
                    listQuery(data),
                    countQuery(data)
            )

    private fun listQuery(data: QuerySourceData): CriteriaQuery<Any> {
        val criteria = cb.createQuery()
        val from = criteria.from(data.root)
        val orderField = from.get<Any>("id")
        return criteria.select(from).where(getPredicate(data, from)).orderBy(cb.desc(orderField))
    }

    private fun countQuery(data: QuerySourceData): CriteriaQuery<Any> {
        val criteria = cb.createQuery()
        val from = criteria.from(data.root)
        return criteria.select(cb.count(from)).where(getPredicate(data, from))
    }

    private fun getPredicate(data: QuerySourceData, from: Root<out Any>) =
            fieldsContainClause(from, data.query.text, data.containsClauseFields, cb)
}