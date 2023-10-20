package com.nbcamp.tripgo.view.search

import com.nbcamp.tripgo.data.repository.model.KeywordSearchEntity
import com.nbcamp.tripgo.util.APIResponse


interface SearchRepository {
    suspend fun getPlaceBySearch(
        keyword: String,
        contentTypeId: String,
        responseCount: Int
    ): List<KeywordSearchEntity>?
}