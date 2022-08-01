/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting.config

import com.kotlindiscord.kord.extensions.checks.types.Check

public class SimpleVotingConfig(private val builder: Builder) : VotingConfig {
	override suspend fun getCommandChecks(): List<Check<*>> =
		builder.commandChecks

	public class Builder {
		internal val commandChecks: MutableList<Check<*>> = mutableListOf()

		public fun commandCheck(body: Check<*>) {
			commandChecks.add(body)
		}
	}
}

public fun SimpleVotingConfig(body: SimpleVotingConfig.Builder.() -> Unit): SimpleVotingConfig {
	val builder = SimpleVotingConfig.Builder()

	body(builder)

	return SimpleVotingConfig(builder)
}
