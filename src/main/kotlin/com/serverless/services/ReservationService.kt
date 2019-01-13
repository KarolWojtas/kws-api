package com.serverless.services


import com.serverless.domain.ReservationDto
import com.serverless.domain.ReservationOrigin
import com.serverless.mappers.ReservationMapper
import java.time.ZonedDateTime

interface ReservationService{
    fun confirmReservation(id: String): Boolean
    fun unconfirmReservation(id: String): Boolean
    fun specifiedTablesAvailableForTime(moment: ZonedDateTime, tables: HashSet<Int>): Boolean
    fun specifiedTablesAvailableForPeriod(moment: ZonedDateTime, offsetMin: Long = 30, repeatOffset: Int = 2, tables: HashSet<Int> ): Boolean
    fun findTablesReservedForPeriod(moment: ZonedDateTime, offsetMin: Long = 30, repeatOffset: Int = 2): List<Int>
    fun findReservationsForPeriod(confirmed: Boolean = true, startDate: ZonedDateTime, endDate: ZonedDateTime): List<ReservationDto>
    fun findReservationsForMoment(confirmed: Boolean = true, moment: ZonedDateTime): List<ReservationDto>
    fun deleteItemsPastMoment(moment: ZonedDateTime)
    fun saveReservationDto(dto: ReservationDto, origin: ReservationOrigin = ReservationOrigin.USER): Boolean
}
class ReservationServiceImpl(val reservationDao: ReservationDaoService, val mapper: ReservationMapper): ReservationService{

    override fun confirmReservation(id: String): Boolean {
        val resFound = reservationDao.queryById(id)
        return if(resFound !== null){
            resFound.apply { confirmed = true }
            reservationDao.save(resFound)
            true
        } else false
    }

    override fun unconfirmReservation(id: String):Boolean {
        val resFound = reservationDao.queryById(id)
        return if(resFound !== null){
            resFound.apply { confirmed = false }
            reservationDao.save(resFound)
            true
        } else false
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

    override fun deleteItemsPastMoment(moment: ZonedDateTime) = reservationDao.batchDeleteReservations(moment)

    override fun saveReservationDto(dto: ReservationDto, reservationOrigin: ReservationOrigin):Boolean{
        val isAvailable = this.specifiedTablesAvailableForPeriod(moment = dto.date, tables = dto.tables)
        return if(isAvailable){
            reservationDao.save(mapper.reservationDtoToReservation(dto).apply { origin = reservationOrigin })
            true
        } else
            false
    }

}