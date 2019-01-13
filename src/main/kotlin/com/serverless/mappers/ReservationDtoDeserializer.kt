package com.serverless.mappers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.serverless.domain.ReservationDto

class ReservationDtoDeserializer(t: Class<ReservationDto>?): StdDeserializer<ReservationDto>(t){

    constructor() : this(null)

    val paramConverter = ParamConverterImpl()
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ReservationDto {
        val dto = ReservationDto()
        val node: JsonNode? = p?.codec?.readTree(p)
        dto.id = null
        dto.created = null
        dto.date = paramConverter.convertTimeParam(node?.get("date")?.asText()?:throw JsonParseException(p, "Date cannot be null"))
        dto.email = node.get("email")?.textValue()
        dto.description = node.get("description")?.asText()
        dto.seats = node.get("seats")?.numberValue() as Int
        dto.confirmed = node.get("confirmed")?.asBoolean()
        dto.tables.addAll(node.withArray("tables").map { it.numberValue() as Int })
        return dto
    }
}