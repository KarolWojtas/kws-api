package com.serverless.handlers.responses

import com.serverless.Response

data class InputResponse(val input: Map<String, Any>) : Response()