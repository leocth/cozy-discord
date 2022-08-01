/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting.config

import com.kotlindiscord.kord.extensions.checks.types.Check

public interface VotingConfig {
	/**
	 * Get the configured staff command checks, used to ensure a staff-facing command can be run.
	 */
	public suspend fun getCommandChecks(): List<Check<*>>
}
