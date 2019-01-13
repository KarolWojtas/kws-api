package com.serverless.domain

import com.amazonaws.services.dynamodbv2.datamodeling.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.serverless.config.DynamoDBAdapter
import com.serverless.mappers.ReservationDtoDeserializer
import com.serverless.mappers.ReservationDtoSerializer
import com.serverless.mappers.ZonedDateTimeConverter
import java.time.ZonedDateTime
import java.util.*

@DynamoDBTable(tableName = "Reservation")
data class Reservation(
        @DynamoDBAttribute(attributeName = "Tables")
        var tables: HashSet<Int> = hashSetOf(),
        @DynamoDBAttribute(attributeName = "Date_Time")
        @DynamoDBTypeConverted(converter = ZonedDateTimeConverter::class)
        @DynamoDBIndexRangeKey(globalSecondaryIndexName = DynamoDBAdapter.CONFIRMED_INDEX)
        @DynamoDBRangeKey
        var date: ZonedDateTime? = null){
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    @DynamoDBAttribute(attributeName = "Id")
    var id: String? = null
    @DynamoDBAttribute(attributeName = "Email")
    var email: String? = null
    @DynamoDBAttribute(attributeName = "Description")
    var description: String? = null
    @DynamoDBAttribute(attributeName = "Origin")
    @DynamoDBTypeConvertedEnum
    var origin: ReservationOrigin = ReservationOrigin.USER
    @DynamoDBAttribute(attributeName = "Seats")
    var seats: Int = 0
    @DynamoDBAttribute(attributeName = "Created")
    @DynamoDBAutoGeneratedTimestamp
    var created: Date? = null
    @DynamoDBAttribute(attributeName = "Confirmed")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = DynamoDBAdapter.CONFIRMED_INDEX)
    var confirmed: Boolean = true

    constructor() : this(tables = hashSetOf(), date = ZonedDateTime.now())

    override fun toString(): String {
        return "Reservation[id: $id, tables: $tables, date: $date, email: $email, desc: $description, origin: $origin, seats: $seats, created: $created, confirmed: $confirmed]"
    }
}
@JsonSerialize(using = ReservationDtoSerializer::class)
@JsonDeserialize(using = ReservationDtoDeserializer::class)
data class ReservationDto(var tables: HashSet<Int>, var date: ZonedDateTime, var seats: Int){
    var email: String? = null
    var description: String? = null
    var id: String? = null
    var created: Date? = null
    var confirmed: Boolean? = true
    constructor(): this(tables = hashSetOf(), date = ZonedDateTime.now(), seats = 0)
}

enum class ReservationOrigin{
    USER, WORKER
}