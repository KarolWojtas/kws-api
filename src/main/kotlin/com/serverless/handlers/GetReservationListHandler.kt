package com.serverless.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.github.salomonbrys.kodein.instance
import com.serverless.ApiGatewayResponse
import com.serverless.config.kodein
import com.serverless.handlers.responses.ReservationListResponse
import com.serverless.mappers.ParameterConverter
import com.serverless.services.ReservationService
import java.time.ZoneId
import java.time.ZonedDateTime

class GetReservationListHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse>, InnerHandler{
    private val resService = kodein.instance<ReservationService>()
    private val paramConverter = kodein.instance<ParameterConverter>()
    private val zoneId = kodein.instance<ZoneId>("warsawZoneId")

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse
            = HandlerUtils.withErrorHandler(input, this::handle)

    override fun handle(input: Map<String, Any>): ApiGatewayResponse{
        val dateParam = ((input[HandlerUtils.QUERY_PARAMS] as Map<String, Any>?)?.get("date")) as String?
        val timeParam = ((input[HandlerUtils.QUERY_PARAMS] as Map<String, Any>?)?.get("time")) as String?
        val confirmedParam = ((input[HandlerUtils.QUERY_PARAMS] as Map<String, Any>?)?.get("confirmed")) as String?

        val confirmed = confirmedParam != "false"

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
}