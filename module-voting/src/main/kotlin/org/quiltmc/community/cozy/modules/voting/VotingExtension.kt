/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("MagicNumber") // You're very funny Detekt

package org.quiltmc.community.cozy.modules.voting

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.*
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralSelectMenu
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.storage.StorageType
import com.kotlindiscord.kord.extensions.storage.StorageUnit
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.utils.hasRole
import com.kotlindiscord.kord.extensions.utils.toDuration
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import org.koin.core.component.inject
import org.quiltmc.community.cozy.modules.voting.config.VotingConfig
import org.quiltmc.community.cozy.modules.voting.data.VotingData
import kotlin.math.ceil
import kotlin.time.Duration.Companion.days

public class VotingExtension : Extension() {
	override val name: String = VotingPlugin.id

	private val config: VotingConfig by inject()
	private val data: VotingData by inject()

	private val votingRolesStorage = StorageUnit(
		StorageType.Config,
		"cozy-quilt",
		"voting-roles",
		VotingRoles::class
	)

	override suspend fun setup() {
		// /vote-menu for:@leocth#3409 position:moderator
		publicSlashCommand {
			name = "vote-menu"
			description = "Commands for creating and configuring vote menus."

			config.getCommandChecks().forEach(::check)

			check { anyGuild() }

			roleSubCommand("eligible") { it.eligibleRoles }
			roleSubCommand("ineligible") { it.ineligibleRoles }
			roleSubCommand("tiebreaker") { it.tiebreakerRoles }

			ephemeralSubCommand(::MarkInactiveArguments) {
				name = "mark-inactive"
				description = "Mark someone as being inactive."

				action {
					val storage = votingRolesStorage.withGuild(guild!!.id)
					val roles = storage.get() ?: storage.save(VotingRoles())
					roles.inactiveMembers.add(arguments.member.id)

					respond { content = "${arguments.member.tag} has been marked as **inactive**." }
				}
			}
			ephemeralSubCommand(::MarkInactiveArguments) {
				name = "unmark-inactive"
				description = "Unmark someone as being inactive."

				action {
					val storage = votingRolesStorage.withGuild(guild!!.id)
					val roles = storage.get() ?: storage.save(VotingRoles())
					roles.inactiveMembers.remove(arguments.member.id)

					respond { content = "${arguments.member.tag} is no longer marked as **inactive**." }
				}
			}

			publicSubCommand(::CreateArguments) {
				name = "create"
				description = "Creates a vote menu."

				check {
					val guild = guildFor(event)!!
					val storage = votingRolesStorage.withGuild(guild)
					val roles = storage.get() ?: storage.save(VotingRoles())

					failIf("There must be at least one eligible role for voting.") {
						roles.eligibleRoles.isEmpty()
					}
					failIf("There must be at least one tiebreaker role for voting.") {
						roles.tiebreakerRoles.isEmpty()
					}
				}

				action {
					val guild = guild!!
					val roles = votingRolesStorage.withGuild(guild.id).get()!!

					val eligibleRoles = roles.eligibleRoles.map { guild.getRole(it) }
					val ineligibleRoles = roles.ineligibleRoles.map { guild.getRole(it) }
					val inactiveMembers = roles.inactiveMembers.map { guild.getMember(it) }

					var ineligibleCount = 0
					var inactiveCount = 0
					val eligibleMembers = guild.members.filter { member ->
						if (!eligibleRoles.any { member.hasRole(it) }) {
							// not eligible to begin with, nope
							false
						} else if (ineligibleRoles.any { member.hasRole(it) }) {
							// has ineligible role, nope
							ineligibleCount += 1
							false
						} else if (member in inactiveMembers) {
							// is marked as inactive, nope
							inactiveCount += 1
							false
						} else {
							true
						}
					}.toList()

					val effectiveCount = eligibleMembers.size
					val eligibleCount = effectiveCount + inactiveCount

					val shortCircuitFactor = 0.75f
					val shortCircuitPercentage = shortCircuitFactor * 100.0f
					val shortCircuitMargin = ceil(effectiveCount.toFloat() * shortCircuitFactor).toInt()

					println(arguments.duration)

					respond {
						content = roles.eligibleRoles.map {
							guild.getRoleOrNull(it)?.mention
						}.joinToString(" ")

						embed {
							title = "Vote"
							author {
								name = arguments.target.tag
								icon = arguments.target.avatar?.url
							}
							field {
								name = "__**Description**__"
								value = makeDescription(arguments)
							}
							field {
								name = "__**Voter Calculations**__"
								value =
									"**Ineligible accounts:** $ineligibleCount\n" +
											"**Eligible accounts:** $eligibleCount\n" +
											"**Plural system adjustment:** TODO\n" +
											"**Inactive user adjustment:** $inactiveCount\n" +
											"**Effective total votes:** $effectiveCount\n\n" +
											"**Remember:** Due to the faultiness of our current voting system, " +
											"missing votes will not be counted as abstentions!\n"
							}
							field {
								name = "__**Short-circuit margins**__"
								value = """
									Failure: $shortCircuitMargin negative votes ($shortCircuitPercentage%)
									Success: $shortCircuitMargin positive votes ($shortCircuitPercentage%)

								""".trimIndent()
							}
							field {
								name = "__**Results after TODO (TODO)**__"
								value = """
									[Failure criteria]

									[Success criteria]

								""".trimIndent()
							}
							field {
								name = "__**Current votes**__"
								value = """
								**11** out of **15** effective votes have been cast.

								👍 **7**   |   👎 **2**   |   🤷 **2**

							""".trimIndent()
							}
							footer {
								val us = user.asUser()
								text = "Initiated by ${us.tag}"
								icon = us.avatar?.url
							}
						}
						components {
							publicButton {
								emoji("\uD83D\uDC4D")
								label = "Positive"
								action {
									respondEphemeral {
										content = "Your vote has been registered."
									}
								}
							}
							publicButton {
								emoji("\uD83D\uDC4E")
								label = "Negative"
								action {
									respondEphemeral {
										content = "Your vote has been registered."
									}
								}
							}
							publicButton {
								emoji("\uD83E\uDD37")
								label = "Abstain"
								action {
									respondEphemeral {
										content = "Your vote has been registered."
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private fun makeDescription(args: CreateArguments): String {
		val desc = StringBuilder()

		if (args.isPlural) {
			desc.append(
				"**This application concerns a plural system.** " +
				"Due to the nature of most plural systems, " +
				"they're applying together, for the same position.\n\n"
			)
		}
		desc.append(
			"This is an application for the position of ${args.position.mention}. " +
			"The application is available on the ModMail server - " +
			"if you haven't read over it, please do so and feel free to ask the applicant " +
			"any questions you feel are poignant. Internal discussion can happen there as well.\n"
		)
		args.modmailThread?.let {
			desc.append("\n**ModMail thread:** $it")
		}
		return desc.toString()
	}

	private suspend inline fun PublicSlashCommand<*>.roleSubCommand(
		roleType: String,
		crossinline access: (VotingRoles) -> MutableSet<Snowflake>
	) {
		ephemeralSubCommand {
			name = roleType
			description = "Sets the $roleType roles for voting."

			check { anyGuild() }
			check { hasPermission(Permission.ManageGuild) }

			action {
				val guild = guild!!
				val storage = votingRolesStorage.withGuild(guild.id)
				val roles = storage.get() ?: storage.save(VotingRoles())

				respond {
					components {
						ephemeralSelectMenu {
							placeholder = "Select roles..."
							maximumChoices = null
							minimumChoices = 0

							guild.roles.collect {
								option(
									label = "@${it.name}",
									value = it.id.toString()
								)
							}
							action {
								access(roles).let {
									it.clear()
									selected.mapTo(it, ::Snowflake)
								}
								storage.save(roles)
								respond {
									val capitalizedType = roleType.replaceFirstChar(Char::uppercaseChar)
									content = "$capitalizedType roles have been updated."
								}
							}
						}
					}
				}
			}
		}
	}

	public inner class CreateArguments : Arguments() {
		public val target: User by user {
			name = "applicant"
			description = "The applicant the vote is for"
		}
		public val position: Role by role {
			name = "position"
			description = "The position the applicant is applying for"
			validate {
				failIf("The role you selected, ${value.mention} is not a valid position for internal voting.") {
					value.id !in VALID_POSITIONS
				}
			}
		}
		public val duration: DateTimePeriod by duration {
			name = "duration"
			description = "How long the vote should last, between 1 to 7 days"
			validate {
				val dt = value.toDuration(TimeZone.UTC)
				failIf("The duration for the vote cannot be shorter than one day") {
					dt < 1.days
				}
				failIf("The duration for the vote cannot be longer than one week (seven days)") {
					dt > 7.days
				}
			}
		}
		public val isPlural: Boolean by defaultingBoolean {
			name = "is-plural"
			description = "Whether the applicant is a plural system"
			defaultValue = false
		}
		public val modmailThread: String? by optionalString {
			name = "modmail-thread"
			description = "(Optional) Link to the application's ModMail thread"
		}
	}

	public inner class MarkInactiveArguments : Arguments() {
		public val member: Member by member {
			name = "member"
			description = "Member to be marked inactive"
		}
	}
}

@Suppress("UnderscoresInNumericLiterals") // TODO will yeet soon
private val VALID_POSITIONS: List<Snowflake> = listOf(1003354152149188608, 1003538431890178118).map(::Snowflake)
