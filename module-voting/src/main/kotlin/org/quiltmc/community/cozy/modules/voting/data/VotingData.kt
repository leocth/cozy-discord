/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.voting.data

import dev.kord.common.entity.Snowflake

public interface VotingData {
	public suspend fun getApplication(id: Snowflake): Application?
	public suspend fun getApplicationsForUser(id: Snowflake): List<Application>
	public suspend fun setApplication(application: Application)
}
