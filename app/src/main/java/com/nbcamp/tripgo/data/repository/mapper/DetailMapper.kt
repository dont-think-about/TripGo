package com.nbcamp.tripgo.data.repository.mapper

import android.text.Html
import com.nbcamp.tripgo.data.model.detailcommon.DetailCommonItem
import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.keywords.KeywordItem
import com.nbcamp.tripgo.data.repository.model.CalendarEntity
import com.nbcamp.tripgo.data.repository.model.DetailCommonEntity

object DetailMapper {
    fun DetailCommonItem.toDetailCommonEntity() = DetailCommonEntity(
        title = title,
        description = Html.fromHtml(overview, Html.FROM_HTML_MODE_LEGACY).toString(),
        telPhoneNumber = tel.ifEmpty { "정보가 없습니다." },
        homePage = Html.fromHtml(homepage, Html.FROM_HTML_MODE_LEGACY).toString()
            .ifEmpty { "정보가 없습니다." },
        mainAddress = addr1,
        subAddress = addr2,
        imageUrl = firstimage,
        latitude = mapy,
        longitude = mapx
    )

    fun KeywordItem.toCalendarEntity(
        startDate: String,
        endDate: String,
        detailInfo: DetailCommonEntity
    ) = CalendarEntity(
        id = null,
        contentId = contentid,
        startDate = startDate,
        endDate = endDate,
        title = title,
        description = detailInfo.description,
        telPhone = detailInfo.telPhoneNumber,
        address = addr1 + addr2,
        homePage = detailInfo.homePage,
        isReviewed = false
    )

    fun FestivalItem.toCalendarEntity(
        startDate: String,
        endDate: String,
        detailInfo: DetailCommonEntity
    ) = CalendarEntity(
        id = null,
        contentId = contentid,
        startDate = startDate,
        endDate = endDate,
        title = title,
        description = detailInfo.description,
        telPhone = detailInfo.telPhoneNumber,
        address = addr1 + addr2,
        homePage = detailInfo.homePage,
        isReviewed = false
    )

}
