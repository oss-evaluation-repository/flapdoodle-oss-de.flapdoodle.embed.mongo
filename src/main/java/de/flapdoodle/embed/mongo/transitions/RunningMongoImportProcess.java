package de.flapdoodle.embed.mongo.transitions;

import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ProcessControl;
import de.flapdoodle.embed.process.types.RunningProcess;

import java.nio.file.Path;

public class RunningMongoImportProcess extends RunningProcess {

	public RunningMongoImportProcess(ProcessControl process, ProcessOutput processOutput, Path pidFile, long timeout) {
		super(process, processOutput, pidFile, timeout);
	}

}