package com.nbcamp.tripgo.view.home.valuetype

@Suppress("SpellCheckingInspection")
enum class AreaCode(areaCode: String, name: String, tourListCount: Int) {
    SEOUL("1", "서울시", 737),
    INCHEON("2", "인천광역시", 423),
    DAEJEON("3", "대전광역시", 132),
    DAEGU("4", " 대구광역시", 312),
    GWANGJU("5", "광주광역시", 161),
    BUSAN("6", "부산광역시", 327),
    ULSAN("7", "울산광역시", 172),
    SEJONG("8", "세종시", 52),
    GYEONGGI("31", " 경기도", 1548),
    GANGWON("32", "강원도", 1371),
    CHUNGBUK("33", "충청북도", 734),
    CHUNGNAM("34", "충청남도", 855),
    GYEONGBUK("35", "경상북도", 1290),
    GYEONGNAM("36", "경상남도", 1372),
    JEONBUK("37", " 전라북도", 976),
    JEONNAM("38", " 전라남도", 1295),
    JEJU("39", "제주도", 519),
}
