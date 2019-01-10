package mappers

import com.github.salomonbrys.kodein.instance
import com.serverless.config.kodein
import com.serverless.mappers.ParameterConverter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestReporter
import org.junit.jupiter.api.assertAll

class ParamConverterTest{
    val converter = kodein.instance<ParameterConverter>()
    @Test
    fun `should convert date`(){
        val year = 2019
        val month = 9
        val day = 21
        val date = converter.convertDateParam("$year-0$month-$day")
        assertAll(
                { assertEquals(year, date.year)},
                { assertEquals(month, date.month.value)},
                { assertEquals(day, date.dayOfMonth)}
        )

    }
    @Test
    fun `should convert millis to date`(testReporter: TestReporter){
        val millis = System.currentTimeMillis()
        val date = converter.convertTimeParam("$millis")
        testReporter.publishEntry(date.toString())
    }
}