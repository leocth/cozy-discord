/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:UseSerializers(UUIDSerializer::class)

package org.quiltmc.community.database.entities

import com.github.jershell.kbson.UUIDSerializer
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.quiltmc.community.cozy.modules.voting.BallotBox
import org.quiltmc.community.cozy.modules.voting.data.Vote
import org.quiltmc.community.database.Entity
import java.util.*

@Serializable
data class VoteEntity(
	override val _id: UUID,

	val applicant: Snowflake,
	val ballotBox: BallotBox,
	val closeTime: Instant,
	val guildId: Snowflake,
	val isClosed: Boolean,
) : Entity<UUID> {

	fun toVote() = Vote(
		id = _id,
		applicant = applicant,
		ballotBox = ballotBox,
		closeTime = closeTime,
		guildId = guildId,
		isClosed = isClosed,
	)
	companion object {
		fun fromVote(app: Vote) = VoteEntity(
			_id = app.id,
			applicant = app.applicant,
			ballotBox = app.ballotBox,
			closeTime = app.closeTime,
			guildId = app.guildId,
			isClosed = app.isClosed
		)
	}
}
