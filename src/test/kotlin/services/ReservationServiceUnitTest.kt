package services

import com.github.salomonbrys.kodein.instance
import com.serverless.config.kodein
import com.serverless.domain.Reservation
import com.serverless.domain.ReservationOrigin
import com.serverless.domain.ReservationStage
import com.serverless.services.ReservationDaoService
import com.serverless.services.ReservationService
import com.serverless.services.ReservationServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.mockito.BDDMockito.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class ReservationServiceUnitTest{
    @Mock
    lateinit var resDao: ReservationDaoService
    lateinit var resService: ReservationServiceImpl
    val offsetMin = 30L
    val time1 = ZonedDateTime.of(2019, 1,1,20,30,0,0, kodein.instance("warsawZoneId"))
    val time2 = time1.plusMinutes(offsetMin)
    val time3 = time1.plusMinutes(offsetMin*2)
    val reservation1 = Reservation(hashSetOf(1,2), time1).apply {
        code = "0987"
        origin = ReservationOrigin.WORKER
        seats = 4
    }
    val reservation5 = Reservation(hashSetOf(1,2), time2).apply {
        code = "0987"
        origin = ReservationOrigin.WORKER
        seats = 4
    }
    val reservation2 = Reservation(hashSetOf(3,4), time1).apply {
        code = "0987"
        origin = ReservationOrigin.USER
        seats = 4
    }

    val reservation3 = Reservation(hashSetOf(3,4), time2).apply {
        code = "0987"
        origin = ReservationOrigin.WORKER
        seats = 4
    }
    val reservation4 = Reservation(hashSetOf(3,4), time3).apply {
        code = "0987"
        origin = ReservationOrigin.WORKER
        seats = 4
    }

    init {
        MockitoAnnotations.initMocks(this)
        resService = ReservationServiceImpl(this.resDao)
    }
    @Test
    fun `mocks initialized`(){
        assertNotNull(resService)
    }
    @Test
    fun `should specify if tables are available for given time`(){
        val resList = listOf(reservation1, reservation2)

        given(resDao.queryAllConfirmedForDateTimeMoment(anyBoolean(), any(ZonedDateTime::class.java)?: ZonedDateTime.now()))
                .willReturn(resList)

        val tablesAvailable = resService.specifiedTablesAvailableForTime(time1, hashSetOf(1,3))
        assertFalse(tablesAvailable)

        val tablesAvailable2 = resService.specifiedTablesAvailableForTime(time1, hashSetOf(11, 9))
        assertTrue(tablesAvailable2)
    }
    @Test
    fun `should return if tables are available for given period`(){
        val resList = listOf(reservation1, reservation2, reservation3, reservation4, reservation5)
        given(resDao.queryAllConfirmedForDateTimePeriod(confirmed = anyBoolean(), startDate = any(ZonedDateTime::class.java)?: ZonedDateTime.now(),
                endDate = any(ZonedDateTime::class.java)?: ZonedDateTime.now())).willReturn(resList)
        val testAvailable1 = resService.specifiedTablesAvailableForPeriod(moment = time2, tables = hashSetOf(1, 9))
        assertFalse(testAvailable1)
        val testAvailable2 = resService.specifiedTablesAvailableForPeriod(moment = time2, tables = hashSetOf(5, 9))
        assertTrue(testAvailable2)
        val testAvailable3 = resService.specifiedTablesAvailableForPeriod(moment = time3, tables = hashSetOf(3,4))
        assertFalse(testAvailable3)

    }
    @Test
    fun `should return if tables`(testReporter: TestReporter){
        val resList = listOf(reservation1, reservation2, reservation3, reservation4, reservation5)

        given(resDao.queryAllConfirmedForDateTimePeriod(confirmed = anyBoolean(), startDate = any(ZonedDateTime::class.java)?: ZonedDateTime.now(),
                endDate = any(ZonedDateTime::class.java)?: ZonedDateTime.now())).willReturn(resList)
        val reservedTables = resService.findTablesReservedForPeriod(time2)
        testReporter.publishEntry(reservedTables.toString())
        assertNotNull(reservedTables)
    }
}