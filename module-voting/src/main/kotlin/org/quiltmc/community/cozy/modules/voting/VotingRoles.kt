/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting

import com.kotlindiscord.kord.extensions.storage.Data
import com.kotlindiscord.kord.extensions.utils.hasRole
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable

@Serializable
@Suppress("DataClassContainsFunctions") // why not?
public data class VotingRoles(
	val eligibleRoles: MutableSet<Snowflake> = mutableSetOf(),
	val ineligibleRoles: MutableSet<Snowflake> = mutableSetOf(),
	val tiebreakerRoles: MutableSet<Snowflake> = mutableSetOf(),
	val inactiveMembers: MutableSet<Snowflake> = mutableSetOf(),

	val validPositions: MutableSet<Snowflake> = mutableSetOf(),
) : Data {
	public suspend fun calculate(guild: GuildBehavior): VoterCalculations {
		val eligibleRoles = eligibleRoles.map { guild.getRole(it) }
		val ineligibleRoles = ineligibleRoles.map { guild.getRole(it) }
		val inactiveMembers = inactiveMembers.map { guild.getMember(it) }

		var ineligibleCount = 0
		var inactiveAdjustment = 0
		val pluralAdjustment = 0 /* TODO */
		val eligibleMembers = guild.members.filter { member ->
			if (!eligibleRoles.any { member.hasRole(it) }) {
				// not eligible to begin with, nope
				false
			} else if (ineligibleRoles.any { member.hasRole(it) }) {
				// has ineligible role, nope
				ineligibleCount += 1
				false
			} else if (member in inactiveMembers) {
				// is marked as inactive, nope
				inactiveAdjustment += 1
				false
			} else {
				true
			}
		}.toList()
		val effectiveCount = eligibleMembers.size
		val eligibleCount = effectiveCount + inactiveAdjustment

		return VoterCalculations(
			eligibleMembers,
			ineligibleCount,
			eligibleCount,
			pluralAdjustment,
			inactiveAdjustment
		)
	}
}
