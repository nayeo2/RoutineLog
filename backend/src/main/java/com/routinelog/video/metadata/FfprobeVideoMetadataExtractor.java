package com.routinelog.video.metadata;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FfprobeVideoMetadataExtractor implements VideoMetadataExtractor {

	private final String ffprobePath;

	public FfprobeVideoMetadataExtractor(@Value("${video.ffprobe-path}") String ffprobePath) {
		this.ffprobePath = ffprobePath;
	}

	@Override
	public BigDecimal extractDuration(Path file) {
		Process process = null;
		try {
			process = new ProcessBuilder(
				ffprobePath,
				"-v", "error",
				"-select_streams", "v:0",
				"-show_entries", "stream=codec_type:format=format_name,duration",
				"-of", "default=noprint_wrappers=1",
				file.toString()
			)
				.redirectErrorStream(true)
				.start();

			if (!process.waitFor(10, TimeUnit.SECONDS)) {
				throw new BusinessException(ErrorCode.VIDEO_UPLOAD_FAILED);
			}
			if (process.exitValue() != 0) {
				throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
			}
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
				.trim();
			String formatName = findValue(output, "format_name");
			String duration = findValue(output, "duration");
			boolean hasVideoStream = output.lines()
				.anyMatch("codec_type=video"::equals);
			boolean isMp4 = formatName != null && Arrays.asList(formatName.split(","))
				.contains("mp4");
			if (!hasVideoStream || !isMp4 || duration == null) {
				throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
			}
			return new BigDecimal(duration);
		} catch (NumberFormatException exception) {
			throw new BusinessException(ErrorCode.INVALID_VIDEO_FILE);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new BusinessException(ErrorCode.VIDEO_UPLOAD_FAILED);
		} catch (IOException exception) {
			throw new BusinessException(ErrorCode.VIDEO_UPLOAD_FAILED);
		} finally {
			if (process != null && process.isAlive()) {
				process.destroyForcibly();
			}
		}
	}

	private String findValue(String output, String key) {
		String prefix = key + "=";
		return output.lines()
			.filter(line -> line.startsWith(prefix))
			.map(line -> line.substring(prefix.length()))
			.findFirst()
			.orElse(null);
	}
}
