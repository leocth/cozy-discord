/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import kotlin.math.ceil

@Suppress("MagicNumber", "DataClassContainsFunctions") // You're very funny Detekt
public data class VoterCalculations(
	val eligibleMembers: List<Member>,

	val ineligibleCount: Int,
	val eligibleCount: Int,
	val pluralAdjustment: Int,
	val inactiveAdjustment: Int,
) {
	// TODO: make this configurable?
	public val effectiveCount: Int get() = eligibleMembers.size
	public val shortCircuitFactor: Float = 0.75f
	public val shortCircuitMargin: Int = ceil(effectiveCount.toFloat() * shortCircuitFactor).toInt()

	public fun makeEmbedBody(): String = """
**Ineligible accounts:** $ineligibleCount
**Eligible accounts:** $eligibleCount
**Plural system adjustment:** $pluralAdjustment
**Inactive user adjustment:** $inactiveAdjustment
**Effective total votes:** $effectiveCount

**Remember:** Due to the faultiness of our current voting system, missing votes will not be counted as abstentions!
	"""

	public fun makeShortCircuitBody(): String {
		val shortCircuitPercentage = shortCircuitFactor * 100.0f
		// TODO: Migrate to i18n
		val votesString = if (shortCircuitMargin == 1) "vote" else "votes"
		return """
			Failure: $shortCircuitMargin negative $votesString ($shortCircuitPercentage%)
			Success: $shortCircuitMargin positive $votesString ($shortCircuitPercentage%)
		""".trimIndent()
	}

	public fun makeInitialVotes(): BallotBox {
		val votes = mutableMapOf<Snowflake, VoteType?>()
		eligibleMembers.forEach {
			votes[it.id] = null
		}
		return BallotBox(votes)
	}
}
