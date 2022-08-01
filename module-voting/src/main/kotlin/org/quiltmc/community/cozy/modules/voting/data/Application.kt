/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting.data

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import kotlinx.serialization.Serializable

@Serializable
public data class Application(
	val id: Snowflake,
	val applicant: Snowflake,
	// eligible voter snowflake => vote
	val votes: Votes
)

public enum class Vote {
	POSITIVE, NEGATIVE, ABSTAIN
}

@Serializable
@JvmInline
public value class Votes(private val inner: Map<Snowflake, Vote?>) {
	public operator fun get(user: User): Vote? = inner[user.id]

	public fun isEligibleVoter(user: User): Boolean = inner.containsKey(user.id)
	public fun hasUserVoted(user: User): Boolean = inner[user.id] != null
}
