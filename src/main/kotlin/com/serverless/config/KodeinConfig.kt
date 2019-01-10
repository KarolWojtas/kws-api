package com.serverless.config

import com.github.salomonbrys.kodein.*
import com.serverless.mappers.ReservationMapper
import com.serverless.mappers.ReservationMapperImpl
import com.serverless.mappers.ZonedDateTimeConverter
import com.serverless.services.ReservationService
import com.serverless.services.ReservationServiceImpl

val kodein = Kodein{
    constant(tag = "dynamoDbHost") with  "http://localhost:8000"
    bind<DynamoDBAdapter>() with singleton { DynamoDBAdapter() }
    bind<ZonedDateTimeConverter>() with singleton { ZonedDateTimeConverter() }
    bind<ReservationService>() with singleton { ReservationServiceImpl(instance()) }
    bind<ReservationMapper>() with singleton { ReservationMapperImpl() }
}