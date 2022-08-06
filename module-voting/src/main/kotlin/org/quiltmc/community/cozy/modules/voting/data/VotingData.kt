/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting.data

import dev.kord.common.entity.Snowflake
import java.util.UUID

public interface VotingData {
	public suspend fun getVote(id: UUID, guildId: Snowflake): Vote?
	public suspend fun getAllActiveVotes(): List<Vote>
	public suspend fun setVote(vote: Vote)
}
