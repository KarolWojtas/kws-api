package com.serverless.handlers.responses

import com.serverless.Response

data class ErrorResponse(
        val code: Int, val message: String
): Response()