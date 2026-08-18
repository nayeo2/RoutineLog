package com.routinelog.video.metadata;

import java.math.BigDecimal;
import java.nio.file.Path;

public interface VideoMetadataExtractor {

	BigDecimal extractDuration(Path file);
}
