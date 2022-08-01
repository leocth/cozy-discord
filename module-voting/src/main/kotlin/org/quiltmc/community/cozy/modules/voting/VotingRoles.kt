/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting

import com.kotlindiscord.kord.extensions.storage.Data
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
public data class VotingRoles(
	val eligibleRoles: MutableSet<Snowflake> = mutableSetOf(),
	val ineligibleRoles: MutableSet<Snowflake> = mutableSetOf(),
	val tiebreakerRoles: MutableSet<Snowflake> = mutableSetOf(),
	val inactiveMembers: MutableSet<Snowflake> = mutableSetOf(),
) : Data
