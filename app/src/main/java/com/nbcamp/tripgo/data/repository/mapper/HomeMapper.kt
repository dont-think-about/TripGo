package com.nbcamp.tripgo.data.repository.mapper

import com.nbcamp.tripgo.data.model.festivals.FestivalItem
import com.nbcamp.tripgo.data.model.travelers.TravlerItem
import com.nbcamp.tripgo.data.model.weathers.WeatherItem
import com.nbcamp.tripgo.data.repository.model.FestivalEntity
import com.nbcamp.tripgo.data.repository.model.TravelerEntity
import com.nbcamp.tripgo.data.repository.model.WeatherEntity

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

    fun List<WeatherItem>?.toWeatherEntity(): WeatherEntity {
        val info = this?.map { it.category to it.obsrValue }

        return WeatherEntity(
            temperature = info?.find { it.first == "T1H" }?.second ?: "0",
            weatherType = getWeatherType(info?.find { it.first == "PTY" }?.second ?: "0"),
            precipitationPerHours = info?.find { it.first == "RN1" }?.second ?: "0"
        )
    }

    private fun getWeatherType(type: String) = when (type) {
        "0" -> WeatherType.SUNNY
        "1" -> WeatherType.RAIN
        "2" -> WeatherType.RAIN_OR_SNOW
        "3" -> WeatherType.SNOW
        "5" -> WeatherType.RAIN_DROP
        "6" -> WeatherType.RAIN_SNOW_DROP
        "7" -> WeatherType.SNOW_FLYING
        else -> WeatherType.UNDEFINED
    }
}
