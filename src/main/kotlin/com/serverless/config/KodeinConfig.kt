package com.serverless.config

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.AWSLambdaAsync
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.*
import com.serverless.mappers.*
import com.serverless.services.*
import java.time.ZoneId
import java.time.ZoneOffset

val kodein = Kodein{
    constant(tag ="warsawZoneId") with ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1))
    constant(tag = "mailFunctionName") with "sendMailKwsFunction"
    constant(tag = "mailTopicArn") with "arn:aws:sns:eu-central-1:306375953143:kws-api-mail-topic"
    bind<DynamoDBAdapter>() with singleton { DynamoDBAdapter() }
    bind<ZonedDateTimeConverter>() with singleton { ZonedDateTimeConverter() }
    bind<ReservationDaoService>() with singleton { ReservationDaoServiceImpl(instance()) }
    bind<ReservationService>() with singleton { ReservationServiceImpl(instance(), instance()) }
    bind<ReservationMapper>() with singleton { ReservationMapperImpl() }
    bind<ParameterConverter>() with singleton { ParamConverterImpl() }
    bind<ObjectMapper>() with singleton { ObjectMapper().apply { registerModule(ReservationFullModule()) } }
    bind<AWSLambdaAsync>() with singleton { AWSLambdaAsyncClientBuilder.defaultClient()}
    bind<AWSLambda>() with singleton { AWSLambdaClientBuilder.defaultClient() }
}