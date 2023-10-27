package com.nbcamp.tripgo.view.calendar

import com.nbcamp.tripgo.data.repository.model.CalendarEntity

interface CalendarRepository {

    /**
     * @return 로그인 한 사용자의 정보 (firebase or KaKao)
     */
    fun getCurrentUser(): Any?

    /**
     *  @param email 로그인한 사용자의 이메일
     *  @return 사용자의 일정 목록
     */
    suspend fun getMySchedules(email: String): List<CalendarEntity>

    /**
     * @param id 삭제할 일정 문서 아이디
     * @return 사용자의 일정 목록
     */
    suspend fun deleteSchedule(id: String): List<CalendarEntity>
}
