/**
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
package de.flapdoodle.embed.mongo.config;

import de.flapdoodle.embed.mongo.packageresolver.Command;
import de.flapdoodle.embed.mongo.commands.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.packageresolver.PlatformPackageResolver;
import de.flapdoodle.embed.mongo.transitions.*;
import de.flapdoodle.embed.mongo.types.DatabaseDir;
import de.flapdoodle.embed.mongo.types.DistributionBaseUrl;
import de.flapdoodle.embed.process.archives.ArchiveType;
import de.flapdoodle.embed.process.archives.ExtractedFileSet;
import de.flapdoodle.embed.process.config.ImmutableRuntimeConfig;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.SupportConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.DistributionDownloadPath;
import de.flapdoodle.embed.process.config.store.DownloadConfig;
import de.flapdoodle.embed.process.config.store.ImmutableDownloadConfig;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.DirectoryAndExecutableNaming;
import de.flapdoodle.embed.process.extract.NoopTempNaming;
import de.flapdoodle.embed.process.extract.UUIDTempNaming;
import de.flapdoodle.embed.process.io.directories.Directory;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.PropertyOrPlatformTempDir;
import de.flapdoodle.embed.process.io.directories.UserHome;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import de.flapdoodle.embed.process.nio.Directories;
import de.flapdoodle.embed.process.nio.directories.PersistentDir;
import de.flapdoodle.embed.process.nio.directories.TempDir;
import de.flapdoodle.embed.process.runtime.CommandLinePostProcessor;
import de.flapdoodle.embed.process.store.*;
import de.flapdoodle.embed.process.transitions.DownloadPackage;
import de.flapdoodle.embed.process.transitions.ExtractPackage;
import de.flapdoodle.embed.process.transitions.InitTempDirectory;
import de.flapdoodle.embed.process.types.Name;
import de.flapdoodle.embed.process.types.ProcessConfig;
import de.flapdoodle.embed.process.types.ProcessEnv;
import de.flapdoodle.os.Platform;
import de.flapdoodle.reverse.*;
import de.flapdoodle.reverse.transitions.Derive;
import de.flapdoodle.reverse.transitions.Join;
import de.flapdoodle.reverse.transitions.Start;
import de.flapdoodle.types.Try;
import org.slf4j.Logger;

import java.util.*;

public abstract class Defaults {

	public static Transition<ExtractedFileSet> extractedFileSetFor(
		StateID<ExtractedFileSet> destination,
		StateID<Distribution> distributionStateID,
		StateID<TempDir> tempDirStateID,
		StateID<Command> commandStateID,
		StateID<DistributionBaseUrl> distributionBaseUrlStateID
	) {
		StateID<Distribution> localDistributionStateID = StateID.of(Distribution.class);
		StateID<TempDir> localTempDirStateID = StateID.of(TempDir.class);
		StateID<Command> localCommandStateID = StateID.of(Command.class);

		PersistentDir baseDir = PersistentDir.userHome(".embedmongo").get();
		DownloadCache downloadCache = new LocalDownloadCache(baseDir.value().resolve("archives"));
		ExtractedFileSetStore extractedFileSetStore = new ContentHashExtractedFileSetStore(baseDir.value().resolve("fileSets"));

		Transitions transitions = Transitions.from(
			Derive.given(localCommandStateID).state(Name.class).deriveBy(c -> Name.of(c.commandName())).withTransitionLabel("name from command"),

			PackageOfCommandDistribution.withDefaults()
				.withDistributionBaseUrl(distributionBaseUrlStateID),

			DownloadPackage.with(downloadCache),

			ExtractPackage.withDefaults()
				.withExtractedFileSetStore(extractedFileSetStore)
		);

		return transitions.walker()
			.asTransitionTo(TransitionMapping.builder("extract file set", StateMapping.of(StateID.of(ExtractedFileSet.class), destination))
				.addMappings(StateMapping.of(distributionStateID, localDistributionStateID))
				.addMappings(StateMapping.of(tempDirStateID, localTempDirStateID))
				.addMappings(StateMapping.of(commandStateID, localCommandStateID))
				.build());
	}

	public static Transitions workspaceDefaults() {
		return Transitions.from(
			InitTempDirectory.withPlatformTempRandomSubDir(),
			Start.to(DistributionBaseUrl.class)
				.initializedWith(DistributionBaseUrl.of("https://fastdl.mongodb.org"))
		);
	}

	public static Transitions versionAndPlatform() {
		return Transitions.from(
			Start.to(Platform.class).providedBy(Platform::detect),
			Join.given(de.flapdoodle.embed.process.distribution.Version.class).and(Platform.class).state(Distribution.class)
				.deriveBy(Distribution::of)
				.withTransitionLabel("version + platform")
		);
	}

	public static Transitions processDefaults() {
		return Transitions.from(
			Start.to(ProcessConfig.class).initializedWith(ProcessConfig.defaults()).withTransitionLabel("create default"),
			Start.to(ProcessEnv.class).initializedWith(ProcessEnv.of(Collections.emptyMap())).withTransitionLabel("create empty env"),

			Derive.given(Name.class).state(ProcessOutput.class)
				.deriveBy(name -> ProcessOutput.namedConsole(name.value()))
				.withTransitionLabel("create named console"),

			Derive.given(Command.class).state(SupportConfig.class)
				.deriveBy(c -> SupportConfig.builder()
					.name(c.commandName())
					.messageOnException((clazz, ex) -> null)
					.supportUrl("https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues")
					.build()).withTransitionLabel("create default")
			);
	}

	public static Transitions commandName() {
		return Transitions.from(
			Derive.given(Command.class).state(Name.class).deriveBy(c -> Name.of(c.commandName())).withTransitionLabel("name from command")
		);
	}

	public static Transitions transitionsForMongoImport(de.flapdoodle.embed.process.distribution.Version version) {
		return MongoImport.instance().transitions(version);
	}

	public static Transitions transitionsForMongoDump(de.flapdoodle.embed.process.distribution.Version version) {
		return MongoDump.instance().transitions(version);
	}

	public static Transitions transitionsForMongoRestore(de.flapdoodle.embed.process.distribution.Version version) {
		return MongoRestore.instance().transitions(version);
	}

	public static Transitions transitionsForMongoShell(de.flapdoodle.embed.process.distribution.Version version) {
		return MongoShell.instance().transitions(version);
	}

	public static Transitions transitionsForMongod(de.flapdoodle.embed.process.distribution.Version version) {
		return Mongod.instance().transitions(version);
	}

	public static Transitions transitionsForMongos(de.flapdoodle.embed.process.distribution.Version version) {
		return Mongos.instance().transitions(version);
	}

	public static ImmutableExtractedArtifactStore extractedArtifactStoreFor(Command command) {
		return ExtractedArtifactStore.builder()
			.downloadConfig(Defaults.downloadConfigFor(command).build())
			.downloader(new UrlConnectionDownloader())
			.extraction(DirectoryAndExecutableNaming.builder()
				.directory(new UserHome(".embedmongo/extracted"))
				.executableNaming(new NoopTempNaming())
				.build())
			.temp(DirectoryAndExecutableNaming.builder()
				.directory(new PropertyOrPlatformTempDir())
				.executableNaming(new UUIDTempNaming())
				.build())
			.build();
	}

	public static ImmutableDownloadConfig.Builder downloadConfigFor(Command command) {
		return DownloadConfigDefaults.defaultsForCommand(command);
	}

	public static ImmutableDownloadConfig.Builder downloadConfigDefaults() {
		return DownloadConfigDefaults.withDefaults();
	}

	protected static class DownloadConfigDefaults {
		protected static ImmutableDownloadConfig.Builder defaultsForCommand(Command command) {
			return withDefaults().packageResolver(packageResolver(command));
		}

		protected static ImmutableDownloadConfig.Builder withDefaults() {
			return DownloadConfig.builder()
				.fileNaming(new UUIDTempNaming())
				.downloadPath(new StaticDownloadPath())
				.progressListener(new StandardConsoleProgressListener())
				.artifactStorePath(defaultArtifactStoreLocation())
				//.downloadPrefix("embedmongo-download")
				.userAgent("Mozilla/5.0 (compatible; Embedded MongoDB; +https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de)");
		}

		public static PackageResolver packageResolver(Command command) {
			return new PlatformPackageResolver(command);
		}

		private static Directory defaultArtifactStoreLocation() {
			return defaultArtifactStoreLocation(System.getenv());
		}

		protected static Directory defaultArtifactStoreLocation(Map<String, String> env) {
			Optional<String> artifactStoreLocationEnvironmentVariable = Optional.ofNullable(env.get("EMBEDDED_MONGO_ARTIFACTS"));
			if (artifactStoreLocationEnvironmentVariable.isPresent()) {
				return new FixedPath(artifactStoreLocationEnvironmentVariable.get());
			} else {
				return new UserHome(".embedmongo");
			}
		}

		private static class StaticDownloadPath implements DistributionDownloadPath {

			@Override
			public String getPath(Distribution distribution) {
				return "https://fastdl.mongodb.org";
			}

		}

	}

	public static ImmutableRuntimeConfig.Builder runtimeConfigFor(Command command, Logger logger) {
		return RuntimeConfigDefaults.defaultsWithLogger(command, logger);
	}

	public static ImmutableRuntimeConfig.Builder runtimeConfigFor(Command command) {
		return RuntimeConfigDefaults.defaults(command);
	}

	protected static class RuntimeConfigDefaults {

		protected static ImmutableRuntimeConfig.Builder defaultsWithLogger(Command command, Logger logger) {
			DownloadConfig downloadConfig = Defaults.downloadConfigFor(command)
				.progressListener(new Slf4jProgressListener(logger))
				.build();
			return defaults(command)
				.processOutput(MongodProcessOutputConfig.getInstance(command, logger))
				.artifactStore(Defaults.extractedArtifactStoreFor(command).withDownloadConfig(downloadConfig));
		}

		protected static ImmutableRuntimeConfig.Builder defaults(Command command) {
			return RuntimeConfig.builder()
				.processOutput(MongodProcessOutputConfig.getDefaultInstance(command))
				.commandLinePostProcessor(new CommandLinePostProcessor.Noop())
				.artifactStore(Defaults.extractedArtifactStoreFor(command));
		}
	}
}
