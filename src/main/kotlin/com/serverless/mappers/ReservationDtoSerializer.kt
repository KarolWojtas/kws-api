package com.serverless.mappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.serverless.domain.ReservationDto

class ReservationDtoSerializer(t: Class<ReservationDto>?) : StdSerializer<ReservationDto>(t){

    constructor() : this(null)

    override fun serialize(value: ReservationDto?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeStringField("id", value?.id)
        gen?.writeNumberField("date", value?.date?.toInstant()?.toEpochMilli()?:0)
        gen?.writeNumberField("seats", value?.seats?:0)
        gen?.writeStringField("email", value?.email)
        gen?.writeStringField("description", value?.description)
        gen?.writeNumberField("created", value?.created?.time?:0)
        gen?.writeArrayFieldStart("tables")
        value?.tables?.forEach { gen?.writeNumber(it) }
        gen?.writeEndArray()
        gen?.writeEndObject()


    }
}