package com.serverless.config

import com.github.salomonbrys.kodein.*
import com.serverless.mappers.*
import com.serverless.services.*
import java.time.ZoneId
import java.time.ZoneOffset

val kodein = Kodein{
    constant(tag ="warsawZoneId") with ZoneId.ofOffset("UTC", ZoneOffset.ofHours(1))
    bind<DynamoDBAdapter>() with singleton { DynamoDBAdapter() }
    bind<ZonedDateTimeConverter>() with singleton { ZonedDateTimeConverter() }
    bind<ReservationDaoService>() with singleton { ReservationDaoServiceImpl(instance()) }
    bind<ReservationService>() with singleton { ReservationServiceImpl(instance(), instance()) }
    bind<ReservationMapper>() with singleton { ReservationMapperImpl() }
    bind<ParameterConverter>() with singleton { ParamConverterImpl() }
    bind<GmailAdapter>() with singleton { GmailAdapter() }
    bind<MailService>()with singleton { MailServiceImpl(instance()) }
}