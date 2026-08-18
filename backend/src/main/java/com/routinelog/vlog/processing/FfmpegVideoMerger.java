package com.routinelog.vlog.processing;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FfmpegVideoMerger implements VideoMerger {

	private final String ffmpegPath;

	public FfmpegVideoMerger(@Value("${video.ffmpeg-path}") String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	@Override
	public void merge(List<Path> inputFiles, Path outputFile) {
		Path concatFile = outputFile.resolveSibling("concat-" + outputFile.getFileName() + ".txt");
		Process process = null;
		try {
			String content = inputFiles.stream()
				.map(path -> "file '" + escapePath(path.toAbsolutePath().toString()) + "'")
				.reduce((first, second) -> first + System.lineSeparator() + second)
				.orElseThrow(() -> new BusinessException(ErrorCode.NO_VIDEOS_FOR_VLOG));
			Files.writeString(concatFile, content, StandardCharsets.UTF_8);

			process = new ProcessBuilder(
				ffmpegPath,
				"-v", "error",
				"-f", "concat",
				"-safe", "0",
				"-i", concatFile.toString(),
				"-c", "copy",
				"-y", outputFile.toString()
			)
				.redirectOutput(ProcessBuilder.Redirect.DISCARD)
				.redirectError(ProcessBuilder.Redirect.DISCARD)
				.start();

			if (!process.waitFor(2, TimeUnit.MINUTES) || process.exitValue() != 0
				|| !Files.exists(outputFile) || Files.size(outputFile) == 0) {
				throw new BusinessException(ErrorCode.VLOG_GENERATION_FAILED);
			}
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.VLOG_GENERATION_FAILED);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.VLOG_GENERATION_FAILED);
		} finally {
			if (process != null && process.isAlive()) {
				process.destroyForcibly();
			}
			try {
				Files.deleteIfExists(concatFile);
			} catch (IOException ignored) {
				// Temporary workspace cleanup is retried by the caller.
			}
		}
	}

	private String escapePath(String path) {
		return path.replace("'", "'\\''").replace('\\', '/');
	}
}
