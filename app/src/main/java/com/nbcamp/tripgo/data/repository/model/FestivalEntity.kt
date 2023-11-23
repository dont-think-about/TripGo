package com.nbcamp.tripgo.data.repository.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FestivalEntity(
    // 컨텐츠 고유번호
    val contentId: String,
    // 제목
    val title: String,
    // 시작일
    val startDate: String,
    // 종료일
    val endDate: String,
    // 이미지 경로
    val imageUrl: String,
    // 시도 주소
    val address: String
) : Parcelable
