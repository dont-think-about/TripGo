package com.nbcamp.tripgo.data.repository.mapper

import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.travelers.TravlerItem
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity

object HomeMapper {
    fun TravlerItem.toTravelerEntity() = TravelerEntity(
        districtName = areaNm,
        travelCount = touNum.split(".").first().toInt(),
        travelerIdentifier = this.touDivNm
    )

    fun FestivalItem.toFestivalEntity() = FestivalEntity(
        contentId = contentid,
        startDate = eventstartdate,
        endDate = eventenddate,
        imageUrl = firstimage,
        title = title,
        address = addr1
    )
}
