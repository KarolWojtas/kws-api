package services

import com.github.salomonbrys.kodein.instance
import com.serverless.config.kodein
import com.serverless.domain.Reservation
import com.serverless.services.MailService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import java.time.ZonedDateTime

class MailServiceTest{
    val mailService = kodein.instance<MailService>()

    @Test
    fun `should print labels`(testReporter: TestReporter){
        val labels = mailService.testReturnLabels()
        testReporter.publishEntry(labels)
    }
    @Test
    fun `should verify email`(){
        val email1 = mailService.verifyEmail("123.gmail@com")
        assertFalse(email1)
    }
    @Test
    @Disabled
    fun `should send confirmation email`(){
        val reservation = Reservation(tables = hashSetOf(1,2,3), date = ZonedDateTime.now()).apply { email = "karol.wojtas.aim@gmail.com" }
        val success = mailService.sendConfirmationEmail(reservation)
        assertTrue(success)
    }
    @Test
    @Disabled
    fun `should send notification email`(){
        val reservation = Reservation(tables = hashSetOf(1,2,3), date = ZonedDateTime.now()).apply { email = "damn@gmail.com" }
        val success = mailService.sendNotificationEmail(reservation)
        assertTrue(success)
    }
}