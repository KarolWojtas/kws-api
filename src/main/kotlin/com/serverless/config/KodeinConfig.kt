package com.serverless.config

import com.github.salomonbrys.kodein.*
import com.serverless.mappers.*
import com.serverless.services.ReservationDaoService
import com.serverless.services.ReservationDaoServiceImpl
import com.serverless.services.ReservationService
import com.serverless.services.ReservationServiceImpl
import java.time.ZoneId
import java.time.ZoneOffset

val kodein = Kodein{
    constant(tag = "dynamoDbHost") with  if(!System.getenv("DYNAMO_DB_HOST").isNullOrEmpty()) System.getenv("dynamoDbHost") else "http://localhost:8000/"
    constant(tag ="warsawZoneId") with ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1))
    bind<DynamoDBAdapter>() with singleton { DynamoDBAdapter() }
    bind<ZonedDateTimeConverter>() with singleton { ZonedDateTimeConverter() }
    bind<ReservationDaoService>() with singleton { ReservationDaoServiceImpl(instance()) }
    bind<ReservationService>() with singleton { ReservationServiceImpl(instance(), instance()) }
    bind<ReservationMapper>() with singleton { ReservationMapperImpl() }
    bind<ParameterConverter>() with singleton { ParamConverterImpl() }
}