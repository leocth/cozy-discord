/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.database.entities

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import org.quiltmc.community.cozy.modules.voting.data.Application
import org.quiltmc.community.cozy.modules.voting.data.Votes
import org.quiltmc.community.database.Entity

@Serializable
data class ApplicationEntity(
	override val _id: Snowflake,

	val applicant: Snowflake,
	val votes: Votes,
) : Entity<Snowflake> {
	fun toApplication() = Application(
		id = _id,
		applicant = applicant,
		votes = votes
	)
	companion object {
		fun fromApplication(app: Application) = ApplicationEntity(
			_id = app.id,
			applicant = app.applicant,
			votes = app.votes
		)
	}
}
