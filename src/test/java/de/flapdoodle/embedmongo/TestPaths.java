/**
 * Copyright (C) 2011 Michael Mosmann <michael@mosmann.de>
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

package de.flapdoodle.embedmongo;

import junit.framework.TestCase;

import de.flapdoodle.embedmongo.distribution.BitSize;
import de.flapdoodle.embedmongo.distribution.Distribution;
import de.flapdoodle.embedmongo.distribution.Platform;
import de.flapdoodle.embedmongo.distribution.Version;


public class TestPaths extends TestCase {

	public void testPaths() {
		checkPath(new Distribution(Version.V1_6_5, Platform.Windows, BitSize.B32), "win32/mongodb-win32-i386-1.6.5.zip");
		checkPath(new Distribution(Version.V1_6_5, Platform.Windows, BitSize.B64), "win32/mongodb-win32-x86_64-1.6.5.zip");
		checkPath(new Distribution(Version.V1_8_0, Platform.Linux, BitSize.B32), "linux/mongodb-linux-i686-1.8.0.tgz");
		checkPath(new Distribution(Version.V1_8_0, Platform.Linux, BitSize.B64), "linux/mongodb-linux-x86_64-1.8.0.tgz");
		checkPath(new Distribution(Version.V1_8_0, Platform.OS_X, BitSize.B32), "osx/mongodb-osx-i386-1.8.0.tgz");
		checkPath(new Distribution(Version.V1_8_0, Platform.OS_X, BitSize.B64), "osx/mongodb-osx-x86_64-1.8.0.tgz");
	}

	private void checkPath(Distribution distribution, String match) {
		assertEquals(""+distribution, match,Paths.getPath(distribution));
	}

}
