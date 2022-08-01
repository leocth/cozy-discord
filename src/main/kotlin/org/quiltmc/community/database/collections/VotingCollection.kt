/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.database.collections

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.soywiz.korio.dynamic.KDynamic.Companion.map
import dev.kord.common.entity.Snowflake
import org.koin.core.component.inject
import org.litote.kmongo.eq
import org.quiltmc.community.cozy.modules.voting.data.Application
import org.quiltmc.community.cozy.modules.voting.data.VotingData
import org.quiltmc.community.database.Collection
import org.quiltmc.community.database.Database
import org.quiltmc.community.database.entities.ApplicationEntity

class VotingCollection : KordExKoinComponent, VotingData {
	private val database: Database by inject()
	private val col = database.mongo.getCollection<ApplicationEntity>(name)

	override suspend fun getApplication(id: Snowflake): Application? =
		col.findOne(ApplicationEntity::_id eq id)?.toApplication()

	override suspend fun getApplicationsForUser(id: Snowflake): List<Application> =
		col.find(ApplicationEntity::applicant eq id)
			.toList()
			.map(ApplicationEntity::toApplication)

	override suspend fun setApplication(application: Application) {
		col.save(ApplicationEntity.fromApplication(application))
	}

	companion object : Collection("voting")
}
