package services

import com.github.salomonbrys.kodein.*
import com.serverless.domain.Reservation
import com.serverless.domain.ReservationOrigin
import com.serverless.mappers.ZonedDateTimeConverter
import com.serverless.config.DynamoDBAdapter
import com.serverless.config.kodein
import com.serverless.mappers.ParameterConverter
import com.serverless.services.ReservationDaoService
import com.serverless.services.ReservationService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import java.time.*

class ReservationDaoServiceITTest{
    val dynamoDbAdapter = kodein.instance<DynamoDBAdapter>()
    val resDao = kodein.instance<ReservationDaoService>()
    val resService = kodein.instance<ReservationService>()
    val paramConverter = kodein.instance<ParameterConverter>()
    val dateConverter = ZonedDateTimeConverter()
    val time = ZonedDateTime.now()
    val reservation = Reservation(tables = hashSetOf(4,11), date = time).apply {
        origin = ReservationOrigin.USER
        seats = 3
    }

    @Test
    @Disabled
    fun `should create table`(){
        dynamoDbAdapter.createReservationTable()
    }
    @Test
    @Disabled
    fun `should drop table`(){
        dynamoDbAdapter.dropReservationTable()
    }
    @Test
    @Disabled
    fun shouldSaveReservation(){
        resDao.save(reservation)
        println(paramConverter.convertToMillis(time))
        resDao.scanAll().forEach { println(it) }


    }
    @Test
    fun `should convert and unconvert dates`(testReporter: TestReporter){
        val date = ZonedDateTime.now()
        val dateString = this.dateConverter.convert(date)

        testReporter.publishEntry("Date string parsed: ", dateString)

        val dateParsed = this.dateConverter.unconvert(dateString)
        testReporter.publishEntry("Date parsed: ", dateParsed.toString())

        assertEquals(date, dateParsed)
    }
    @Test
    fun `should query reservations for given date period`(){
        val startDate = ZonedDateTime.of(2019,1,9,16,0,0,0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)))
        val endDate = ZonedDateTime.of(2019,1,11,23,0,0,0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)))
        resDao.queryAllConfirmedForDateTimePeriod(true, startDate, endDate).forEach { println(it) }
    }
    @Test
    fun `should find by id`(){
        val reservation = resDao.scanAll().getOrNull(0)
        val reservationFoundByIndex = resDao.queryById(reservation?.id?:"noid")
        assertEquals(reservation, reservationFoundByIndex)
    }
    @Test
    fun `should unconfirm a reservation`(){
        dynamoDbAdapter.dynamoDbMapper.save(reservation)
        val savedReservation = resDao.scanAllForDateTime(reservation.date?: ZonedDateTime.now(), reservation.date?: ZonedDateTime.now()).first()
        assertNotNull(savedReservation)
        resService.unconfirmReservation(savedReservation.id?:"")
        val changedRes = resDao.queryById(savedReservation.id?:"")
        assertFalse(changedRes?.confirmed?:false)

    }
    @Test
    fun `should confirm a reservation`(){
        reservation.confirmed = false
        dynamoDbAdapter.dynamoDbMapper.save(reservation)
        val savedReservation = resDao.scanAllForDateTime(reservation.date?: ZonedDateTime.now(), reservation.date?: ZonedDateTime.now()).first()
        assertNotNull(savedReservation)
        resService.confirmReservation(savedReservation.id?:"")
        val changedRes = resDao.queryById(savedReservation.id?:"")
        assertTrue(changedRes?.confirmed?:false)
    }
    @Test
    fun `should find reservation by specific date and time`(){
        val time = ZonedDateTime.of(2019, 1,1,20,30,10,0, kodein.instance("warsawZoneId"))
        val reservation = Reservation(hashSetOf(1,2,3), time).apply {
            origin = ReservationOrigin.WORKER
            seats = 4
        }
        resDao.save(reservation)
        val resList = resDao.queryAllConfirmedForDateTimeMoment(true, time)
        assertEquals(reservation, resList.first())
    }
    @Test
    fun `should batch delete items`(){
        val time = ZonedDateTime.now()
        resService.deleteItemsPastMoment(time)
        val resList = resService.findReservationsForPeriod(startDate = time.minusDays(3), endDate = time)
        assertEquals(resList, emptyList<Reservation>())
    }

}