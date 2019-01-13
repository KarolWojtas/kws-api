package com.serverless.handlers

import com.fasterxml.jackson.core.JsonParseException
import com.serverless.ApiGatewayResponse
import com.serverless.domain.ReservationDto
import com.serverless.handlers.errors.EmptyRequestException
import com.serverless.handlers.errors.ParameterMissingException
import com.serverless.handlers.responses.ErrorResponse

class HandlerUtils{
    companion object {
        const val QUERY_PARAMS = "queryStringParameters"
        const val PATH_PARAMS = "pathParameters"
        inline fun withErrorHandler(input: Map<String, Any>, block: (Map<String, Any>) -> ApiGatewayResponse): ApiGatewayResponse{
            return try {
                block.invoke(input)
            } catch (e: JsonParseException){
                ApiGatewayResponse.build {
                    statusCode = 400
                    objectBody = ErrorResponse(400, "Error parsing body")
                }
            } catch (e: ParameterMissingException){
                ApiGatewayResponse.build {
                    statusCode = 400
                    objectBody = ErrorResponse(400, "Required parameter missing")
                }
            } catch (e: EmptyRequestException){
                ApiGatewayResponse.build {
                    statusCode = 400
                    objectBody = ErrorResponse(400, "Required request body missing")
                }
            } catch (e: Exception){
                ApiGatewayResponse.build {
                    statusCode = 500
                    objectBody = ErrorResponse(400, "Kws Api. Internal server exception")
                }
            }
        }
    }
}
fun ReservationDto.dtoValidUser(): Boolean{
    var isValid = true
    if(this.email.isNullOrBlank())
        isValid = false
    return isValid
}
interface InnerHandler{
    fun handle(input: Map<String, Any>): ApiGatewayResponse
}
