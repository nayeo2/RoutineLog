package com.routinelog.video.storage;

import com.routinelog.common.exception.BusinessException;
import com.routinelog.common.exception.ErrorCode;
import java.nio.file.Path;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3VideoStorage implements VideoStorage {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Override
	public void upload(String objectKey, Path file, String contentType) {
		if (bucket.isBlank()) {
			throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
		}

		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucket)
				.key(objectKey)
				.contentType(contentType)
				.build();
			s3Client.putObject(request, RequestBody.fromFile(file));
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
		}
	}

	@Override
	public void download(String objectKey, Path destination) {
		try {
			GetObjectRequest request = GetObjectRequest.builder()
				.bucket(bucket)
				.key(objectKey)
				.build();
			s3Client.getObject(request, ResponseTransformer.toFile(destination));
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
		}
	}

	@Override
	public String createPlaybackUrl(String objectKey) {
		if (bucket.isBlank()) {
			throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
		}
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucket)
				.key(objectKey)
				.build();
			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(15))
				.getObjectRequest(getObjectRequest)
				.build();
			return s3Presigner.presignGetObject(presignRequest).url().toString();
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
		}
	}

	@Override
	public void delete(String objectKey) {
		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(objectKey)
				.build();
			s3Client.deleteObject(request);
		} catch (RuntimeException exception) {
			throw new BusinessException(ErrorCode.S3_UPLOAD_FAILED);
		}
	}
}
