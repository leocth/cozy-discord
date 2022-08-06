/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.common.entity.Snowflake
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.litote.kmongo.or
import org.quiltmc.community.cozy.modules.voting.data.Vote
import org.quiltmc.community.cozy.modules.voting.data.VotingData
import org.quiltmc.community.database.Collection
import org.quiltmc.community.database.Database
import org.quiltmc.community.database.entities.VoteEntity
import java.util.UUID

class VotingCollection : KordExKoinComponent, VotingData {
	private val database: Database by inject()
	private val col = database.mongo.getCollection<VoteEntity>(name)

	override suspend fun getVote(id: UUID, guildId: Snowflake): Vote? =
		col.findOne(
			or(VoteEntity::guildId eq null, VoteEntity::guildId eq guildId),
			VoteEntity::_id eq id,
		)?.toVote()

	override suspend fun getAllActiveVotes(): List<Vote> =
		col.find(
			VoteEntity::isClosed eq false
		)
			.toList()
			.map { it.toVote() }

	override suspend fun setVote(vote: Vote) {
		col.save(VoteEntity.fromVote(vote))
	}

	companion object : Collection("voting")
}
