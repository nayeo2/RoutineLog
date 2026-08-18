package com.routinelog.video.storage;

import java.nio.file.Path;

public interface VideoStorage {

	void upload(String objectKey, Path file, String contentType);

	void download(String objectKey, Path destination);

	String createPlaybackUrl(String objectKey);

	void delete(String objectKey);
}
