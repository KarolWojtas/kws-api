package com.serverless.handlers.errors

import java.lang.Exception

class EmptyRequestException(message: String? = null) : Exception(message)