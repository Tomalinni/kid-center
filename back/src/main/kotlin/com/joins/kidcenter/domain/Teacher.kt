package com.joins.kidcenter.domain

import javax.persistence.Entity

@Entity
class Teacher() : BasicEntity() {
    var name: String = ""
}