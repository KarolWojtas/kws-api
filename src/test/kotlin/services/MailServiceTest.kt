package services

import com.github.salomonbrys.kodein.instance
import com.serverless.config.kodein
import com.serverless.services.MailService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter

class MailServiceTest{
    val mailService = kodein.instance<MailService>()

    @Test
    fun `should print labels`(testReporter: TestReporter){
        val labels = mailService.testReturnLabels()
        testReporter.publishEntry(labels)
    }
}