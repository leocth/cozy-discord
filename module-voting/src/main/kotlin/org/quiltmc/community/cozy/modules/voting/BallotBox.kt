/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

// Fine, do you seriously expect me to make a `VotingFormatter` later on?
@Suppress("DataClassContainsFunctions")
@Serializable
public data class BallotBox(
	private val votes: MutableMap<Snowflake, VoteType?>
) {
	private val tally: MutableMap<VoteType, Int> = tallyVotes()

	public operator fun get(id: Snowflake): VoteType? = votes[id]

	public fun isVoter(id: Snowflake): Boolean = votes.containsKey(id)

	public fun vote(id: Snowflake, voteType: VoteType): Result.Vote {
		if (!isVoter(id)) return Result.InvalidVoter
		votes[id]?.let {
			return Result.AlreadyVoted(it)
		}

		votes[id] = voteType
		tally[voteType] = tally[voteType]?.plus(1) ?: 1

		return Result.Ok
	}

	public fun retract(id: Snowflake): Result.Retract {
		if (!isVoter(id)) return Result.InvalidVoter
		val oldVote = votes[id] ?: return Result.HasntVoted

		votes[id] = null
		tally[oldVote] = tally[oldVote]?.minus(1) ?: 0

		return Result.Ok
	}

	private fun tallyVotes(): MutableMap<VoteType, Int> {
		val tally = mutableMapOf(
			VoteType.Positive to 0,
			VoteType.Negative to 0,
			VoteType.Abstain to 0,
		)
		votes.values.forEach {
			if (it != null) {
				tally[it] = tally[it]?.plus(1) ?: 0
			}
		}
		return tally
	}

	public fun display(): String {
		val totalVotes = tally.values.sum()
		val totalPossibleVotes = votes.entries.count()
		val prefix = "**$totalVotes** out of **$totalPossibleVotes** effective votes have been cast.\n\n"

		return tally.entries.joinToString(
			prefix = prefix,
			separator = " | "
		) { (vote, count) ->
			"${vote.emoji} **$count**"
		}
	}
}

public sealed interface Result {
	public sealed interface Vote : Result {
		public val voteMessage: String
	}
	public sealed interface Retract : Result {
		public val retractMessage: String
	}

	public val isSuccess: Boolean

	public object Ok : Vote, Retract {
		override val voteMessage: String = "Your vote has been registered."
		override val retractMessage: String = "Vote successfully retracted."
		override val isSuccess: Boolean = true
	}
	public object InvalidVoter : Vote, Retract {
		override val voteMessage: String = "Only eligible voters may vote."
		override val retractMessage: String = voteMessage
		override val isSuccess: Boolean = false
	}
	public data class AlreadyVoted(public val voteType: VoteType) : Vote {
		override val voteMessage: String = "You have already voted for ${voteType.displayName}!"
		override val isSuccess: Boolean = false
	}
	public object HasntVoted : Retract {
		override val retractMessage: String = "You must vote before retracting your vote!"
		override val isSuccess: Boolean = false
	}
}

@Serializable
public enum class VoteType(
	public val emoji: String,
	public val displayName: String,
) {
	Positive("\uD83D\uDC4D", "Positive"),
	Negative("\uD83D\uDC4E", "Negative"),
	Abstain("\uD83E\uDD37", "Abstain"),
}
