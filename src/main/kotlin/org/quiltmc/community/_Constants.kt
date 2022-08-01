/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("MagicNumber", "UnderscoresInNumericLiterals")

package org.quiltmc.community

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake

internal val DISCORD_TOKEN = env("TOKEN")
internal val GITHUB_TOKEN = envOrNull("GITHUB_TOKEN")

internal val MAIN_GUILD = Snowflake(
	envOrNull("MAIN_GUILD_ID")?.toULong()
		?: envOrNull("COMMUNITY_GUILD_ID")?.toULong()
		?: 817576132726620200U
)

internal val MESSAGE_LOG_CATEGORIES = envOrNull("MESSAGE_LOG_CATEGORIES")?.split(',')
	?.map { Snowflake(it.trim()) } ?: listOf()

internal val COLOUR_BLURPLE = DISCORD_BLURPLE
internal val COLOUR_NEGATIVE = DISCORD_RED
internal val COLOUR_POSITIVE = DISCORD_GREEN

internal val COMMUNITY_MODERATOR_ROLE = snowflake("COMMUNITY_MODERATOR_ROLE", 863767207716192306)
internal val COMMUNITY_DEVELOPER_ROLE = snowflake("COMMUNITY_DEVELOPER_ROLE", 972868531844710412)
internal val COMMUNITY_COMMUNITY_TEAM_ROLE = snowflake("COMMUNITY_COMMUNITY_TEAM_ROLE", 863710574650327100)

internal val TOOLCHAIN_MODERATOR_ROLE = snowflake("TOOLCHAIN_MODERATOR_ROLE", 863767485609541632)
internal val TOOLCHAIN_DEVELOPER_ROLE = snowflake("TOOLCHAIN_DEVELOPER_ROLE", 849305976951537725)

internal val MODERATOR_ROLES: List<Snowflake> =
	(envOrNull("MODERATOR_ROLES") ?: envOrNull("COMMUNITY_MANAGEMENT_ROLES")) // For now, back compat
		?.split(',')
		?.map { Snowflake(it.trim()) }
		?: listOf(COMMUNITY_MODERATOR_ROLE, TOOLCHAIN_MODERATOR_ROLE)

internal val COMMUNITY_GUILD = snowflake("COMMUNITY_GUILD_ID", 817576132726620200)
internal val TOOLCHAIN_GUILD = snowflake("COMMUNITY_GUILD_ID", 833872081585700874)

internal val GUILDS = envOrNull("GUILDS")?.split(',')?.map { Snowflake(it.trim()) }
	?: listOf(COMMUNITY_GUILD, TOOLCHAIN_GUILD)

internal val SUGGESTION_CHANNEL = snowflake("SUGGESTION_CHANNEL_ID", 832353359074689084)
internal val GALLERY_CHANNEL = snowflake("GALLERY_CHANNEL_ID", 832348385997619300)

internal fun snowflake(env: String, default: Long): Snowflake =
	envOrNull(env)?.let { Snowflake(it) } ?: Snowflake(default)
