package com.joins.kidcenter.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

interface Persistable {
    var id: Long?

    @JsonIgnore
    fun isNew(): Boolean =
            id === null

    @JsonIgnore
    fun safeId(): Long =
            if (id === null) -1 else id!!
}

@MappedSuperclass
abstract class BasicEntity : Persistable {
    @Id @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    override var id: Long? = null
}

@MappedSuperclass
@EntityListeners(*arrayOf(AuditingEntityListener::class))
abstract class AuditableEntity : BasicEntity() {
    @CreatedDate
    @Column(name = "created_date")
    var createdDate: LocalDateTime? = null
    @CreatedBy
    @Column(name = "created_by")
    var createdBy: String? = null
    @LastModifiedDate
    @Column(name = "modified_date")
    var modifiedDate: LocalDateTime? = null
    @LastModifiedBy
    @Column(name = "modified_by")
    var modifiedBy: String? = null
}