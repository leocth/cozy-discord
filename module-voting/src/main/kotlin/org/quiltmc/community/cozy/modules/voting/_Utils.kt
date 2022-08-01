/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import org.koin.dsl.bind
import org.quiltmc.community.cozy.modules.voting.config.SimpleVotingConfig
import org.quiltmc.community.cozy.modules.voting.config.VotingConfig
import org.quiltmc.community.cozy.modules.voting.data.VotingData

public fun ExtensibleBotBuilder.ExtensionsBuilder.voting(
	config: VotingConfig,
	data: VotingData
) {
	loadModule { single { config } bind VotingConfig::class }
	loadModule { single { data } bind VotingData::class }

	add { VotingExtension() }
}

public fun ExtensibleBotBuilder.ExtensionsBuilder.voting(
	data: VotingData,
	body: SimpleVotingConfig.Builder.() -> Unit
) {
	voting(SimpleVotingConfig(body), data)
}
