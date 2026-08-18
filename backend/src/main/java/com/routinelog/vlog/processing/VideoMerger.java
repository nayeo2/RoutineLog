package com.routinelog.vlog.processing;

import java.nio.file.Path;
import java.util.List;

public interface VideoMerger {

	void merge(List<Path> inputFiles, Path outputFile);
}
