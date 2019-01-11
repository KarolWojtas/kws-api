package com.serverless.handlers.responses

import com.serverless.Response

data class TableListResponse(
        val tables: List<Int>,
        val time: String? = null) : Response()