package com.serverless.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.github.salomonbrys.kodein.instance
import com.serverless.ApiGatewayResponse
import com.serverless.Handler
import com.serverless.config.DynamoDBAdapter
import com.serverless.config.kodein
import com.serverless.domain.Reservation
import com.serverless.handlers.responses.InputResponse
import com.serverless.handlers.responses.ReservationListResponse
import com.serverless.mappers.ParamConverterImpl
import com.serverless.mappers.ParameterConverter
import com.serverless.services.ReservationDaoServiceImpl
import com.serverless.services.ReservationService
import com.serverless.services.ReservationServiceImpl
import org.apache.logging.log4j.LogManager
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class GetReservationListHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse>{
    private val resService = kodein.instance<ReservationService>()
    private val paramConverter = kodein.instance<ParameterConverter>()
    private val zoneId = kodein.instance<ZoneId>("warsawZoneId")

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val dateParam = ((input[HandlerUtils.QUERY_PARAMS] as Map<String, Any>?)?.get("date")) as String?
        val timeParam = ((input.get(HandlerUtils.QUERY_PARAMS) as Map<String, Any>?)?.get("time")) as String?
        val confirmedParam = ((input.get("queryStringParameters") as Map<String, Any>?)?.get("confirmed")) as String?

        val confirmed = confirmedParam !== "false"

        val resList =
                if (!dateParam.isNullOrBlank()) {
                    val startDate = paramConverter.convertDateParam(dateParam)

                    resService.findReservationsForPeriod(confirmed = confirmed,
                            startDate = startDate, endDate = startDate.plusDays(1L))

                } else if (!timeParam.isNullOrBlank()) {
                    val moment = paramConverter.convertTimeParam(timeParam)
                    resService.findReservationsForMoment(confirmed = confirmed, moment = moment)
                } else {
                    val startDate = ZonedDateTime.now(zoneId)
                    resService.findReservationsForPeriod(confirmed = confirmed,
                            startDate = startDate, endDate = startDate.plusDays(1L))
                }
        return ApiGatewayResponse.build {
            statusCode = 200
            objectBody = ReservationListResponse(resList)
            headers = mapOf("Access-Control-Allow-Origin" to "*", "Access-Control-Allow-Credentials" to "true")

        }

    }
    companion object {
        private val LOG = LogManager.getLogger(this::class.java)
    }
}