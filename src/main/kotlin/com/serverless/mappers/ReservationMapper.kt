package com.serverless.mappers

import com.serverless.domain.Reservation
import com.serverless.domain.ReservationDto
import org.mapstruct.*
import org.mapstruct.factory.Mappers

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface ReservationMapper{
    companion object {
        val INSTANCE = Mappers.getMapper(ReservationMapper::class.java)
    }
    @Mappings(
            Mapping(target = "tables", source = "tables"),
            Mapping(target = "date", source = "date"),
            Mapping(target = "email", source="email"),
            Mapping(target = "description", source="description"),
            Mapping(target = "id", source="id"),
            Mapping(target = "created", source="created")
    )
    fun reservationToReservationDto(reservation: Reservation): ReservationDto
    @InheritInverseConfiguration
    fun reservationDtoToReservation(reservationDto: ReservationDto): Reservation

}