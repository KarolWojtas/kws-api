package com.serverless.handlers.responses

import com.serverless.Response

data class TableListResponse(
        val tables: HashSet<Int>,
        val date: String? = null,
        val time: String? = null) : Response()