/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.loadModule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.koin.dsl.bind
import org.quiltmc.community.cozy.modules.voting.config.SimpleVotingConfig
import org.quiltmc.community.cozy.modules.voting.config.VotingConfig
import org.quiltmc.community.cozy.modules.voting.data.VotingData
import java.util.*

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

internal object UUIDSerializer : KSerializer<UUID> {
	override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
	override fun serialize(encoder: Encoder, value: UUID) {
		encoder.encodeString(value.toString())
	}
}
