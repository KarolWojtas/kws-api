package mappers
import com.serverless.domain.Reservation
import com.serverless.domain.ReservationDto
import com.serverless.mappers.ReservationMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import org.junit.jupiter.api.assertAll
import java.time.ZonedDateTime

class ReservationMapperTest{
    val mapper = ReservationMapper.INSTANCE
    @Test
    fun `should map reservation to dto`(){
        val reservation = Reservation().apply {
            date = ZonedDateTime.now()
            tables = hashSetOf(3,4,5)
            email = "karol@g.com"
            description = null
            seats = 6
        }
        val dto = mapper.reservationToReservaationDto(reservation)
        assertAll(
                { assertEquals(reservation.date, dto.date)},
                { assertEquals(reservation.seats, dto.seats)},
                { assertEquals(reservation.email, dto.email)},
                { assertEquals(reservation.description, dto.description)})
        reservation.tables.forEach {
            assertTrue { dto.tables.contains(it) }
        }
    }
    @Test
    fun `should map dto to reservation`(testReporter: TestReporter){
        val dto = ReservationDto(tables = hashSetOf(1,2,3), date = ZonedDateTime.now(), seats = 3).apply {
            email= "karol@g.com"
            description = "description"
        }
        val res = mapper.reservationDtoToReservation(dto)
        assertAll(
                { assertEquals(res.date, dto.date)},
                { assertEquals(res.seats, dto.seats)},
                { assertEquals(res.email, dto.email)},
                { assertEquals(res.description, dto.description)})
        res.tables.forEach {
            assertTrue { dto.tables.contains(it) }
        }
    }
}