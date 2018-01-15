package com.joins.kidcenter.dto

class SearchResult<T>(
        var results: Collection<T>,
        var total: Long
)