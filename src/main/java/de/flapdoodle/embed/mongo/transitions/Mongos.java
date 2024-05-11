/*
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano	(trajano@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.mongo.commands.MongosArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.flapdoodle.reverse.transitions.Start;
import org.immutables.value.Value;

@Value.Immutable
public class Mongos implements WorkspaceDefaults, VersionAndPlatform, ProcessDefaults, CommandName, ExtractFileSet {
	public Transitions transitions(de.flapdoodle.embed.process.distribution.Version version) {
		return workspaceDefaults()
			.addAll(versionAndPlatform())
			.addAll(processDefaults())
			.addAll(commandNames())
			.addAll(extractFileSet())
			.addAll(
				Start.to(Command.class).initializedWith(Command.MongoS).withTransitionLabel("provide Command"),
				Start.to(de.flapdoodle.embed.process.distribution.Version.class).initializedWith(version),
				Start.to(Net.class).providedBy(Net::defaults),

				mongosArguments(),
				mongosProcessArguments(),
				mongosStarter()
			);
	}

	public Start<MongosArguments> mongosArguments() {
		return Start.to(MongosArguments.class).initializedWith(MongosArguments.defaults());
	}

	@Value.Default
	protected MongosProcessArguments mongosProcessArguments() {
		return MongosProcessArguments.withDefaults();
	}

	@Value.Default
	protected MongosStarter mongosStarter() {
		return MongosStarter.withDefaults();
	}

	public TransitionWalker.ReachedState<RunningMongosProcess> start(Version version) {
		return transitions(version)
			.walker()
			.initState(StateID.of(RunningMongosProcess.class));
	}

	public static ImmutableMongos instance() {
		return builder().build();
	}

	public static ImmutableMongos.Builder builder() {
		return ImmutableMongos.builder();
	}
}
