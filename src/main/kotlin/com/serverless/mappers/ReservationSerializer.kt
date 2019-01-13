package com.serverless.mappers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.serverless.domain.Reservation

class ReservationSerializer(t: Class<Reservation>?) : StdSerializer<Reservation>(t){
    constructor(): this(null)

    override fun serialize(value: Reservation?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeStringField("id", value?.id)
        gen?.writeNumberField("date", value?.date?.toInstant()?.toEpochMilli()?:0)
        gen?.writeNumberField("seats", value?.seats?:0)
        gen?.writeStringField("email", value?.email)
        gen?.writeStringField("description", value?.description)
        gen?.writeNumberField("created", value?.created?.toInstant()?.toEpochMilli()?:0)
        gen?.writeBooleanField("confirmed", value?.confirmed?:false)
        gen?.writeStringField("origin", value?.origin?.name)
        gen?.writeArrayFieldStart("tables")
        value?.tables?.forEach { gen?.writeNumber(it) }
        gen?.writeEndArray()
        gen?.writeEndObject()
    }
}