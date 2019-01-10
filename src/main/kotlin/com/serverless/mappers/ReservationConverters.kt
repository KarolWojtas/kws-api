package com.serverless.mappers

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ZonedDateTimeConverter : DynamoDBTypeConverter<String, ZonedDateTime>{
    override fun unconvert(dateString: String?): ZonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)

    override fun convert(date: ZonedDateTime?): String = DateTimeFormatter.ISO_DATE_TIME.format(date)
}
