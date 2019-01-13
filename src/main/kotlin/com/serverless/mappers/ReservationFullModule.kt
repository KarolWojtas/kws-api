package com.serverless.mappers

import com.fasterxml.jackson.databind.module.SimpleModule
import com.serverless.domain.Reservation
import com.serverless.domain.ReservationDto

class ReservationFullModule : SimpleModule("ReservationDtoDeserializer"){
    init{
        super.addDeserializer(ReservationDto::class.java, ReservationDtoDeserializer())
        super.addSerializer(ReservationDto::class.java, ReservationDtoSerializer())
        super.addSerializer(Reservation::class.java, ReservationSerializer())
    }
}