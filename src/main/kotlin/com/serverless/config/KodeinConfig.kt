package com.serverless.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.salomonbrys.kodein.*
import com.serverless.mappers.*
import com.serverless.services.*
import java.time.ZoneId
import java.time.ZoneOffset

val kodein = Kodein{
    constant(tag ="warsawZoneId") with ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1))
    constant(tag = "mailFunctionName") with "sendMailKwsFunction"
    bind<DynamoDBAdapter>() with singleton { DynamoDBAdapter() }
    bind<ZonedDateTimeConverter>() with singleton { ZonedDateTimeConverter() }
    bind<ReservationDaoService>() with singleton { ReservationDaoServiceImpl(instance()) }
    bind<ReservationService>() with singleton { ReservationServiceImpl(instance(), instance()) }
    bind<ReservationMapper>() with singleton { ReservationMapperImpl() }
    bind<ParameterConverter>() with singleton { ParamConverterImpl() }
    bind<ObjectMapper>() with singleton { ObjectMapper().apply { registerModule(ReservationFullModule()) } }
    bind<MailService>() with singleton { MailServiceImpl() }
}