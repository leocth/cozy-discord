/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.tags.config

import com.kotlindiscord.kord.extensions.checks.types.Check
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.GuildMessageChannel
import org.quiltmc.community.cozy.modules.tags.TagFormatter

/**
 * Interface representing the configuration for the tags module. Extend this and register an instance with Koin to
 * change how the module is configured.
 *
 * All functions are suspending to allow for database access, for example, where needed.
 */
public interface TagsConfig {
	/**
	 * Get the configured tag formatter callback, used to turn a tag into a message. **Users configuring this to avoid
	 * creating embeds should make sure to append to the message content instead of replacing it.**
	 */
	public suspend fun getTagFormatter(): TagFormatter

	/**
	 * Get the configured user command checks, used to ensure a user-facing command can be run.
	 */
	public suspend fun getUserCommandChecks(): List<Check<*>>

	/**
	 * Get the configured staff command checks, used to ensure a staff-facing command can be run.
	 */
	public suspend fun getStaffCommandChecks(): List<Check<*>>

	/**
	 * Get the logging channel for logging tag updates, returning `null` if this isn't needed.
	 */
	public suspend fun getLoggingChannelOrNull(guild: Guild): GuildMessageChannel?

	/**
	 * Function wrapping [getLoggingChannelOrNull], with a non-null assertion.
	 */
	public suspend fun getLoggingChannel(guild: Guild): GuildMessageChannel =
		getLoggingChannelOrNull(guild)!!
}
