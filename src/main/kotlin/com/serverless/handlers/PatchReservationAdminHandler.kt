package com.serverless.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.instance
import com.serverless.ApiGatewayResponse
import com.serverless.config.kodein
import com.serverless.domain.ReservationDto
import com.serverless.handlers.errors.EmptyRequestException
import com.serverless.handlers.responses.ErrorResponse
import com.serverless.services.ReservationService

class PatchReservationAdminHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse>, InnerHandler{

    private val resService = kodein.instance<ReservationService>()
    private val objectMapper = kodein.instance<ObjectMapper>()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse = HandlerUtils.withErrorHandler(input, this::handle)

    override fun handle(input: Map<String, Any>): ApiGatewayResponse {
        val bodyString = input["body"] as String?
        val idParam = ((input[HandlerUtils.PATH_PARAMS] as Map<String, Any>?)?.get("id")) as String?
         if(bodyString.isNullOrBlank() || idParam.isNullOrBlank()){
            throw EmptyRequestException()
        } else {
            val resDto = objectMapper.readValue(bodyString, ReservationDto::class.java)

            var isPatchSuccessful = false
            if(resDto.confirmed != null){
                isPatchSuccessful = if(resDto.confirmed as Boolean) resService.confirmReservation(idParam)
                else resService.unconfirmReservation(idParam)
            }

            return if(isPatchSuccessful){
                ApiGatewayResponse.build {
                    statusCode = 200
                    objectBody = ErrorResponse(code =200, message = "Patch successful")
                    headers = mapOf("Access-Control-Allow-Origin" to "*", "Access-Control-Allow-Credentials" to "true")
                }
            } else {
                ApiGatewayResponse.build {
                    statusCode = 404
                    objectBody = ErrorResponse(code = 404, message = "Patch unsuccessful")
                }
            }

        }
    }
}