/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:UseSerializers(UUIDSerializer::class)

@file:Suppress("ImportOrdering") // You are insane, Detekt. Now what?

package org.quiltmc.community.cozy.modules.voting.data

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.quiltmc.community.cozy.modules.voting.BallotBox
import org.quiltmc.community.cozy.modules.voting.UUIDSerializer

import java.util.UUID

// Well yes, but no
@Suppress("DataClassShouldBeImmutable", "DataClassContainsFunctions")
@Serializable
public data class Vote(
	val id: UUID,
	val applicant: Snowflake,
	val ballotBox: BallotBox,
	val closeTime: Instant,
	val guildId: Snowflake,

	var isClosed: Boolean = false
) {
	public suspend fun onClose() {
		isClosed = true
	}
}
