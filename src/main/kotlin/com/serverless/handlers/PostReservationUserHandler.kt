package com.serverless.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.instance
import com.serverless.ApiGatewayResponse
import com.serverless.config.kodein
import com.serverless.domain.ReservationDto
import com.serverless.handlers.errors.EmptyRequestException
import com.serverless.handlers.errors.ParameterMissingException
import com.serverless.handlers.responses.ErrorResponse
import com.serverless.mappers.ReservationMapper
import com.serverless.services.MailService
import com.serverless.services.ReservationService
import java.util.*

class PostReservationUserHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse>, InnerHandler{
    private val resService = kodein.instance<ReservationService>()
    private val objectMapper = kodein.instance<ObjectMapper>()
    private val mapper = kodein.instance<ReservationMapper>()
    private val mailService = kodein.instance<MailService>()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse
            = HandlerUtils.withErrorHandler(input, this::handle)

    override fun handle(input: Map<String, Any>): ApiGatewayResponse {
        val bodyString = input["body"] as String?
        if(bodyString.isNullOrBlank()){
            throw EmptyRequestException()
        } else {
            val resDto = objectMapper.readValue(bodyString, ReservationDto::class.java)

            if(!resDto.dtoValidUser()){
                throw ParameterMissingException()
            }
            val isSuccessful = resService.saveReservationDto(resDto)


            return if(isSuccessful){
                val reservation = mapper.reservationDtoToReservation(resDto).apply {
                    created = Date()
                }
                mailService.sendConfirmationEmail(reservation)
                mailService.sendNotificationEmail(reservation)

                ApiGatewayResponse.build {
                    statusCode = 200
                    objectBody = ErrorResponse(code =200, message = "Reservation successful")
                    headers = mapOf("Access-Control-Allow-Origin" to "*", "Access-Control-Allow-Credentials" to "true")
                }
            } else {
                ApiGatewayResponse.build {
                    statusCode = 404
                    objectBody = ErrorResponse(code = 404, message = "Reservation not saved, probably because tables not available")
                }
            }

        }
    }
}