package com.serverless.handlers

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.github.salomonbrys.kodein.instance
import com.serverless.ApiGatewayResponse
import com.serverless.config.kodein
import com.serverless.handlers.responses.ErrorResponse
import com.serverless.handlers.responses.TableListResponse
import com.serverless.mappers.ParameterConverter
import com.serverless.services.ReservationService
import org.apache.logging.log4j.LogManager

class GetTableListHandler : RequestHandler<Map<String, Any>, ApiGatewayResponse>{
    private val resService = kodein.instance<ReservationService>()
    private val paramConverter = kodein.instance<ParameterConverter>()

    override fun handleRequest(input: Map<String, Any>, context: Context): ApiGatewayResponse {

        val timeParam = ((input.get(HandlerUtils.PATH_PARAMS) as Map<String, Any>?)?.get("time")) as String?

        return if(timeParam.isNullOrBlank()){
            ApiGatewayResponse.build {
                statusCode = 400
                objectBody = ErrorResponse(code = 400, message = "Time path parameter required")
            }
        } else {
            val moment = paramConverter.convertTimeParam(timeParam)
            val tableList = resService.findTablesReservedForPeriod(moment)
            ApiGatewayResponse.build {
                statusCode = 200
                objectBody = TableListResponse(tables = tableList, time = timeParam)
                headers = mapOf("Access-Control-Allow-Origin" to "*", "Access-Control-Allow-Credentials" to "true")
            }
        }
    }
    companion object {
        private val LOG = LogManager.getLogger(this::class.java)
    }
}