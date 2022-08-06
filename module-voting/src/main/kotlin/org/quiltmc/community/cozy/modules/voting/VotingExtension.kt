/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("MagicNumber") // You're very funny Detekt

package org.quiltmc.community.cozy.modules.voting

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.ephemeralSelectMenu
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.storage.StorageType
import com.kotlindiscord.kord.extensions.storage.StorageUnit
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.toDuration
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import org.koin.core.component.inject
import org.quiltmc.community.cozy.modules.voting.config.VotingConfig
import org.quiltmc.community.cozy.modules.voting.data.Vote
import org.quiltmc.community.cozy.modules.voting.data.VotingData
import java.util.*
import kotlin.time.Duration.Companion.days

public class VotingExtension : Extension() {
	override val name: String = VotingPlugin.id

	private val scheduler: Scheduler = Scheduler()
	private val config: VotingConfig by inject()
	private val data: VotingData by inject()

	private val votingRolesStorage = StorageUnit(
		StorageType.Config,
		"cozy-quilt",
		"voting-roles",
		VotingRoles::class
	)

	override suspend fun setup() {
		data.getAllActiveVotes().forEach {
			val now = Clock.System.now()
			if (it.closeTime > now) {
				// already happened, run now!
				it.onClose()
			} else {
				scheduler.schedule(
					delay = it.closeTime - now,
					name = it.id.toString()
				) {
					it.onClose()
				}
			}
		}

		publicSlashCommand {
			name = "vote"
			description = "Commands for creating and configuring votes."

			config.getCommandChecks().forEach(::check)

			check { anyGuild() }

			roleSubCommand("eligible") { it.eligibleRoles }
			roleSubCommand("ineligible") { it.ineligibleRoles }
			roleSubCommand("tiebreaker") { it.tiebreakerRoles }
			roleSubCommand("valid-positions", roleName = "valid positions") {
				it.validPositions
			}

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
				description = "Creates a vote."

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
					val result = roles.calculate(guild)

					val id = UUID.randomUUID()
					val duration = arguments.duration.toDuration(TimeZone.UTC)
					val closeTime = Clock.System.now() + duration
					val ballotBox = result.makeInitialVotes()

					val vote = Vote(
						id = id,
						applicant = arguments.applicant.id,
						ballotBox = ballotBox,
						closeTime = closeTime,
						guildId = guild.id
					)
					data.setVote(vote)

					scheduler.schedule(duration, name = id.toString()) { vote.onClose() }

					respond {
						content = roles.eligibleRoles.map {
							guild.getRoleOrNull(it)?.mention
						}.joinToString(" ")

						embed {
							title = "Vote"
							author {
								name = arguments.applicant.tag
								icon = arguments.applicant.avatar?.url
							}
							description = makeDescription(arguments)
							field {
								name = "__**Voter Calculations**__"
								value = result.makeEmbedBody()
							}
							field {
								name = "__**Short-circuit margins**__"
								value = result.makeShortCircuitBody()
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
								value = ballotBox.display()
							}
							footer {
								text = "Initiated by ${user.asUser().tag} | ID: $id"
								icon = user.asUser().avatar?.url
							}
						}
						components {
							voteButton(id, VoteType.Positive)
							voteButton(id, VoteType.Negative)
							voteButton(id, VoteType.Abstain)

							publicButton {
								label = "Retract"
								style = ButtonStyle.Danger
								action {
									val saidVote = data.getVote(id, guild.id)!!
									val voteResult = saidVote.ballotBox.retract(member!!.id)
									if (voteResult.isSuccess) data.setVote(vote)

									respondEphemeral {
										embed {
											color = if (voteResult.isSuccess) DISCORD_BLURPLE else DISCORD_RED
											description = voteResult.retractMessage
										}
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
		roleName: String = roleType,
		crossinline access: (VotingRoles) -> MutableSet<Snowflake>
	) {
		ephemeralSubCommand {
			name = roleType
			description = "Sets the $roleName roles for voting."

			check { anyGuild() }
			check { hasPermission(Permission.ManageGuild) }

			action {
				val guild = guild!!
				val storage = votingRolesStorage.withGuild(guild.id)
				val roles = storage.get() ?: storage.save(VotingRoles())

				respond {
					content = "Select the $roleName roles for voting."
					components {
						ephemeralSelectMenu {
							placeholder = "Select roles..."
							maximumChoices = null
							minimumChoices = 0

							guild.roles.collect {
								option(
									label = if (it.guildId == it.id) "@everyone" else "@${it.name}",
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
									val capitalizedName = roleName.replaceFirstChar(Char::uppercaseChar)
									content = "$capitalizedName roles have been updated."
								}
							}
						}
					}
				}
			}
		}
	}

	private suspend fun ComponentContainer.voteButton(
		voteId: UUID,
		voteType: VoteType,
	) {
		publicButton {
			emoji(voteType.emoji)
			label = voteType.displayName
			style = ButtonStyle.Primary
			action {
				val vote = data.getVote(voteId, guild!!.id)!!
				val result = vote.ballotBox.vote(member!!.id, voteType)
				if (result.isSuccess) data.setVote(vote)

				edit {
					val voteDisplayField = embeds?.first()?.fields?.last() ?: return@edit
					voteDisplayField.value = vote.ballotBox.display()
				}
				respondEphemeral {
					embed {
						color = if (result.isSuccess) DISCORD_BLURPLE else DISCORD_RED
						description = result.voteMessage
					}
				}
			}
		}
	}

	public inner class CreateArguments : Arguments() {
		public val applicant: User by user {
			name = "applicant"
			description = "The applicant the vote is for"
		}
		public val position: Role by role {
			name = "position"
			description = "The position the applicant is applying for"

			validate {
				failIf("Not used in a guild!") { context.getGuild() == null }
				failIf("The role you selected, ${value.mention} is not a valid position for internal voting.") {
					val storage = votingRolesStorage.withGuild(context.getGuild()!!)
					val roles = storage.get() ?: storage.save(VotingRoles())

					value.id !in roles.validPositions
				}
			}
		}
		public val duration: DateTimePeriod by defaultingDuration {
			name = "duration"
			description = "How long the vote should last, between 1 to 7 days"
			defaultValue = DateTimePeriod(days = 7)

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
