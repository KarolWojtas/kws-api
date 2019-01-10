package com.serverless.services

import java.time.ZonedDateTime

interface ReservationService{
    fun confirmReservation(id: String)
    fun unconfirmReservation(id: String)
    fun specifiedTablesAvailableForTime(moment: ZonedDateTime, tables: HashSet<Int>): Boolean
    fun specifiedTablesAvailableForPeriod(moment: ZonedDateTime, offsetMin: Long = 30, repeatOffset: Int = 2, tables: HashSet<Int> ): Boolean
    fun findTablesReservedForPeriod(moment: ZonedDateTime, offsetMin: Long = 30, repeatOffset: Int = 2): List<Int>

}
class ReservationServiceImpl(val reservationDao: ReservationDaoService): ReservationService{

    override fun confirmReservation(id: String) {
        val resFound = reservationDao.queryById(id)
        if(resFound !== null){
            resFound?.apply { confirmed = true }
            reservationDao.save(resFound)
        }
    }

    override fun unconfirmReservation(id: String) {
        val resFound = reservationDao.queryById(id)
        if(resFound !== null){
            resFound?.apply { confirmed = false }
            reservationDao.save(resFound)
        }
    }
    override fun specifiedTablesAvailableForTime(moment: ZonedDateTime, tables: HashSet<Int>): Boolean{
        val resList = reservationDao.queryAllConfirmedForDateTimeMoment(confirmed = true, moment = moment)
        return resList.asSequence().flatMap { it.tables.asSequence() }.none { tables.contains(it) }
    }

    override fun specifiedTablesAvailableForPeriod(moment: ZonedDateTime, offsetMin: Long, repeatOffset: Int, tables: HashSet<Int>): Boolean {
        val fullOffset = offsetMin * repeatOffset
        val resList = reservationDao.queryAllConfirmedForDateTimePeriod(confirmed = true, startDate = moment.minusMinutes(fullOffset), endDate = moment.plusMinutes(fullOffset))
        return resList.asSequence().flatMap { it.tables.asSequence() }.distinct().none { tables.contains(it) }
    }

    override fun findTablesReservedForPeriod(moment: ZonedDateTime, offsetMin: Long, repeatOffset: Int): List<Int> {
        val fullOffset = offsetMin * repeatOffset
        val resList = reservationDao.queryAllConfirmedForDateTimePeriod(confirmed = true, startDate = moment.minusMinutes(fullOffset), endDate = moment.plusMinutes(fullOffset))
        return resList.asSequence().flatMap { it.tables.asSequence() }.distinct().toList()
    }
}