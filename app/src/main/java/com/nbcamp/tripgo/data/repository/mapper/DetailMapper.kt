package com.nbcamp.tripgo.data.repository.mapper

import android.text.Html
import com.nbcamp.tripgo.data.model.detailcommon.DetailCommonItem
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

}
