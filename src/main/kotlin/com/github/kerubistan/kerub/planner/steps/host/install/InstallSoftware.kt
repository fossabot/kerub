package com.github.kerubistan.kerub.planner.steps.host.install

import com.fasterxml.jackson.annotation.JsonTypeName
import com.github.kerubistan.kerub.model.Host
import com.github.kerubistan.kerub.planner.OperationalState
import com.github.kerubistan.kerub.planner.reservations.FullHostReservation
import com.github.kerubistan.kerub.planner.reservations.Reservation
import com.github.kerubistan.kerub.planner.steps.AbstractOperationalStep

@JsonTypeName("install-software")
data class InstallSoftware(val packageName: String, val host: Host) : AbstractOperationalStep {

	override fun reservations(): List<Reservation<*>>
			= listOf(FullHostReservation(host))

	override fun take(state: OperationalState): OperationalState {
		throw UnsupportedOperationException()
	}
}