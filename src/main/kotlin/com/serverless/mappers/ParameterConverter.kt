package com.serverless.mappers

import com.github.salomonbrys.kodein.instance
import com.serverless.config.kodein
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField

interface ParameterConverter{
    fun convertDateParam(dateParam: String): ZonedDateTime
    fun convertTimeParam(timeParam: String): ZonedDateTime
    fun convertToMillis(time: ZonedDateTime): String
}
class ParamConverterImpl : ParameterConverter{
    companion object {
        const val DATE_PARAM_FORMAT = "yyyy-MM-dd"
    }
    override fun convertDateParam(dateParam: String): ZonedDateTime{
        val dateAtMidnight = "${dateParam}T00:00:00"
        return LocalDateTime.parse(dateAtMidnight, DateTimeFormatter.ISO_DATE_TIME).atZone(kodein.instance("warsawZoneId"))
    }
    override fun convertTimeParam(timeParam: String): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeParam.toLong()), kodein.instance("warsawZoneId"))

    override fun convertToMillis(time: ZonedDateTime): String = time.toInstant().toEpochMilli().toString()
}