package com.serverless.services

import com.serverless.domain.Reservation
import com.serverless.domain.ReservationDto
import com.serverless.mappers.ReservationMapper
import java.time.ZonedDateTime

interface ReservationService{
    fun confirmReservation(id: String)
    fun unconfirmReservation(id: String)
    fun specifiedTablesAvailableForTime(moment: ZonedDateTime, tables: HashSet<Int>): Boolean
    fun specifiedTablesAvailableForPeriod(moment: ZonedDateTime, offsetMin: Long = 30, repeatOffset: Int = 2, tables: HashSet<Int> ): Boolean
    fun findTablesReservedForPeriod(moment: ZonedDateTime, offsetMin: Long = 30, repeatOffset: Int = 2): List<Int>
    fun findReservationsForPeriod(confirmed: Boolean = true, startDate: ZonedDateTime, endDate: ZonedDateTime): List<ReservationDto>
    fun findReservationsForMoment(confirmed: Boolean = true, moment: ZonedDateTime): List<ReservationDto>
}
class ReservationServiceImpl(val reservationDao: ReservationDaoService, val mapper: ReservationMapper): ReservationService{

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

    override fun findReservationsForPeriod(confirmed: Boolean, startDate: ZonedDateTime, endDate: ZonedDateTime): List<ReservationDto>
            = reservationDao.queryAllConfirmedForDateTimePeriod(confirmed = confirmed, startDate = startDate, endDate = endDate).map (mapper::reservationToReservationDto)

    override fun findReservationsForMoment(confirmed: Boolean, moment: ZonedDateTime): List<ReservationDto>
            = reservationDao.queryAllConfirmedForDateTimeMoment(confirmed = confirmed, moment = moment).map (mapper::reservationToReservationDto)
}