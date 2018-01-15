package com.joins.kidcenter.dto

class ProfileDto() {
    var name: String = ""
    var oldPass: String? = null
    var newPass: String? = null

    constructor(name: String) : this() {
        this.name = name;
    }
}
