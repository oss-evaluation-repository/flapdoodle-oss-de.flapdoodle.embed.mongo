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
package de.flapdoodle.embed.mongo.packageresolver.linux;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.packageresolver.HtmlParserResultTester;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Version;
import de.flapdoodle.os.CommonArchitecture;
import de.flapdoodle.os.ImmutablePlatform;
import de.flapdoodle.os.OS;
import de.flapdoodle.os.Platform;
import de.flapdoodle.os.linux.CentosVersion;
import de.flapdoodle.os.linux.UbuntuVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CentosPackageResolverTest {
  /*
			RedHat / CentOS 7.0 x64
			--
			4.2.4 - 4.2.4, 3.4.8 - 3.4.8, 2.6.12 - 2.6.0
			https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel70-{}.tgz
			5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.5, 4.2.3 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.9, 3.4.7 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0
   */

  @ParameterizedTest
  @ValueSource(strings = {"5.0.2 - 5.0.0", "4.4.9 - 4.4.0", "4.2.16 - 4.2.5", "4.2.3 - 4.2.0", "4.0.26 - 4.0.0", "3.6.22 - 3.6.0", "3.4.23 - 3.4.9", "3.4.7 - 3.4.0", "3.2.21 - 3.2.0", "3.0.14 - 3.0.0"})
  public void centos7(String version) {
    assertThat(linuxWith(CommonArchitecture.X86_64, CentosVersion.CentOS_7), version)
      .resolvesTo("/linux/mongodb-linux-x86_64-rhel70-{}.tgz");
  }

  /*
 			RedHat / CentOS 8.0 x64
			--
			4.2.4 - 4.2.4, 4.2.0 - 4.2.0, 4.0.13 - 4.0.0, 3.6.16 - 3.6.0, 3.4.23 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
			https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel80-{}.tgz
			5.0.2 - 5.0.0, 4.4.9 - 4.4.0, 4.2.16 - 4.2.5, 4.2.3 - 4.2.1, 4.0.26 - 4.0.14, 3.6.22 - 3.6.17
   */

  @ParameterizedTest
  @ValueSource(strings = {"5.0.2 - 5.0.0", "4.4.9 - 4.4.0", "4.2.16 - 4.2.5", "4.2.3 - 4.2.1", "4.0.26 - 4.0.14", "3.6.22 - 3.6.17"})
  public void centos8(String version) {
    assertThat(linuxWith(CommonArchitecture.X86_64, CentosVersion.CentOS_8), version)
      .resolvesTo("/linux/mongodb-linux-x86_64-rhel80-{}.tgz");
  }

  /*
			RedHat / CentOS 8.2 ARM 64
			--
			4.4.3 - 4.4.0, 4.2.16 - 4.2.0, 4.0.26 - 4.0.0, 3.6.22 - 3.6.0, 3.4.23 - 3.4.0, 3.2.21 - 3.2.0, 3.0.14 - 3.0.0, 2.6.12 - 2.6.0
			https://fastdl.mongodb.org/linux/mongodb-linux-aarch64-rhel82-{}.tgz
			5.0.2 - 5.0.0, 4.4.9 - 4.4.4
  */
  @ParameterizedTest
  @ValueSource(strings = {"5.0.2 - 5.0.0", "4.4.9 - 4.4.0"})
  public void centos8arm(String version) {
    assertThat(linuxWith(CommonArchitecture.ARM_64, CentosVersion.CentOS_8), version)
      .resolvesTo("/linux/mongodb-linux-aarch64-rhel82-{}.tgz");
  }

  private static Platform linuxWith(CommonArchitecture architecture, de.flapdoodle.os.Version version) {
    return ImmutablePlatform.builder()
            .operatingSystem(OS.Linux)
            .architecture(architecture)
            .version(version)
            .build();
  }

  private static HtmlParserResultTester assertThat(Platform platform, String versionList) {
    return HtmlParserResultTester.with(
            new CentosPackageResolver(Command.Mongo),
            version -> Distribution.of(Version.of(version), platform),
            versionList);
  }

}