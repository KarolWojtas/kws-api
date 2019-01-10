package com.serverless.handlers.responses

import com.serverless.Response
import com.serverless.domain.Reservation
import com.serverless.domain.ReservationDto

data class ReservationListResponse(val reservationList: List<ReservationDto>) : Response()