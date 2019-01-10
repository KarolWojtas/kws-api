package com.serverless.services

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.github.salomonbrys.kodein.instance
import com.serverless.config.DynamoDBAdapter
import com.serverless.config.kodein
import com.serverless.domain.Reservation
import com.serverless.mappers.ZonedDateTimeConverter
import java.time.ZonedDateTime

interface ReservationDaoService{
    fun save(reservation: Reservation)
    fun queryById(id: String): Reservation?
    fun scanAll(linit: Int = 100): List<Reservation>
    fun scanAllForDateTime(startDate: ZonedDateTime, endDate: ZonedDateTime): List<Reservation>
    fun queryAllConfirmedForDateTimePeriod(confirmed: Boolean, startDate: ZonedDateTime, endDate: ZonedDateTime): List<Reservation>
    fun queryAllConfirmedForDateTimeMoment(confirmed: Boolean, moment: ZonedDateTime): List<Reservation>
}
class ReservationDaoServiceImpl(val dynamoDBAdapter: DynamoDBAdapter) : ReservationDaoService{
    private val startDateParam = ":startDate"
    private val endDateParam = ":endDate"
    private val confirmedParam =":confirmed"
    private val momentParam = ":moment"
    private val timeConverter = kodein.instance<ZonedDateTimeConverter>()
    override fun save(reservation: Reservation) {
        dynamoDBAdapter.dynamoDbMapper.save(reservation)
    }

    override fun queryById(id: String): Reservation? {
        val queryExp = DynamoDBQueryExpression<Reservation>().apply {
            keyConditionExpression = "Id = :id"
            expressionAttributeValues = mapOf(":id" to AttributeValue().withS(id))
        }
        return dynamoDBAdapter.dynamoDbMapper.query(Reservation::class.java, queryExp).firstOrNull()
    }

    override fun scanAll(limit: Int): List<Reservation> {
        val scanAll = DynamoDBScanExpression().apply {
            withLimit(limit)
        }
        return dynamoDBAdapter.dynamoDbMapper.scan(Reservation::class.java, scanAll)
    }

    override fun scanAllForDateTime(startDate: ZonedDateTime, endDate: ZonedDateTime): List<Reservation> {
        val startDateStr = timeConverter.convert(startDate)
        val endDateStr = timeConverter.convert(endDate)

        val scanExp = DynamoDBScanExpression().apply {
            filterExpression = "Date_Time BETWEEN $startDateParam AND $endDateParam"
            expressionAttributeValues = mapOf(startDateParam to AttributeValue().withS(startDateStr), endDateParam to AttributeValue().withS(endDateStr))
        }
        return dynamoDBAdapter.dynamoDbMapper.scan(Reservation::class.java, scanExp)
    }

    override fun queryAllConfirmedForDateTimePeriod(confirmed: Boolean, startDate: ZonedDateTime, endDate: ZonedDateTime): List<Reservation> {
        val startDateStr = timeConverter.convert(startDate)
        val endDateStr = timeConverter.convert(endDate)
        val confirmedStr = confirmed.toNumberString()
        val qExp = DynamoDBQueryExpression<Reservation>().apply {
            indexName = DynamoDBAdapter.CONFIRMED_INDEX
            isConsistentRead = false
            keyConditionExpression = "Confirmed = $confirmedParam AND Date_Time BETWEEN $startDateParam AND $endDateParam"
            expressionAttributeValues = mapOf(confirmedParam to AttributeValue().withN(confirmedStr), startDateParam to AttributeValue().withS(startDateStr), endDateParam to AttributeValue().withS(endDateStr))
        }
        return dynamoDBAdapter.dynamoDbMapper.query(Reservation::class.java, qExp)
    }

    override fun queryAllConfirmedForDateTimeMoment(confirmed: Boolean, moment: ZonedDateTime): List<Reservation> {
        val momentStr = timeConverter.convert(moment)
        val confirmedStr = confirmed.toNumberString()
        val qExp = DynamoDBQueryExpression<Reservation>().apply {
            indexName = DynamoDBAdapter.CONFIRMED_INDEX
            isConsistentRead = false
            keyConditionExpression = "Confirmed = $confirmedParam AND Date_Time = $momentParam"
            expressionAttributeValues = mapOf(confirmedParam to AttributeValue().withN(confirmedStr), momentParam to AttributeValue().withS(momentStr))
        }
        return dynamoDBAdapter.dynamoDbMapper.query(Reservation::class.java, qExp)
    }
    fun Boolean.toNumberString(): String =  if(this) "1" else "0"
}