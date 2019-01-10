import com.github.salomonbrys.kodein.*
import com.serverless.domain.Reservation
import com.serverless.domain.ReservationOrigin
import com.serverless.domain.ReservationStage
import com.serverless.mappers.ZonedDateTimeConverter
import com.serverless.config.DynamoDBAdapter
import com.serverless.services.ReservationService
import com.serverless.config.kodein
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import java.time.*
import java.time.temporal.TemporalAccessor
import java.util.*

class ReservationServiceTest{
    val dynamoDbAdapter = kodein.instance<DynamoDBAdapter>()
    val resService = kodein.instance<ReservationService>()
    val dateConverter = ZonedDateTimeConverter()
    val reservation = Reservation(tables = hashSetOf(1,2,3), date = ZonedDateTime.now()).apply {
        code = "1234"
        origin = ReservationOrigin.USER
        seats = 3
        stage = ReservationStage.START
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
        resService.save(reservation)
        resService.scanAll().forEach { println(it) }


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
        val endDate = ZonedDateTime.of(2019,1,10,23,0,0,0, ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1)))
        resService.queryAllConfirmedForDateTime(true, startDate, endDate).forEach { println(it) }
    }
    @Test
    fun `should find by id`(){
        val reservation = resService.scanAll().getOrNull(0)
        val reservationFoundByIndex = resService.queryById(reservation?.id?:"noid")
        assertEquals(reservation, reservationFoundByIndex)
    }
    @Test
    fun `should unconfirm a reservation`(){
        dynamoDbAdapter.dynamoDbMapper.save(reservation)
        val savedReservation = resService.scanAllForDateTime(reservation.date?: ZonedDateTime.now(), reservation.date?: ZonedDateTime.now()).first()
        assertNotNull(savedReservation)
        resService.unconfirmReservation(savedReservation.id?:"")
        val changedRes = resService.queryById(savedReservation.id?:"")
        assertFalse(changedRes?.confirmed?:false)

    }
    @Test
    fun `should confirm a reservation`(){
        reservation.confirmed = false
        dynamoDbAdapter.dynamoDbMapper.save(reservation)
        val savedReservation = resService.scanAllForDateTime(reservation.date?: ZonedDateTime.now(), reservation.date?: ZonedDateTime.now()).first()
        assertNotNull(savedReservation)
        resService.confirmReservation(savedReservation.id?:"")
        val changedRes = resService.queryById(savedReservation.id?:"")
        assertTrue(changedRes?.confirmed?:false)
    }
}