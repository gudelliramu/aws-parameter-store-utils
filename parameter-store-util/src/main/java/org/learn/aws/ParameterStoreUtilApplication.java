package org.learn.aws;

import org.apache.commons.lang3.StringUtils;
import org.learn.aws.ssm.service.ParameterStoreUtils;
import org.learn.aws.sts.StsCallerIdentity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;

@SpringBootApplication
public class ParameterStoreUtilApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParameterStoreUtilApplication.class, args);
	}

	@Bean
	public RetryRegistry retryRegistry() {
		return RetryRegistry.ofDefaults();
	}

	@Bean
	public RetryConfig retryConfig() {
		return RetryConfig.custom().retryExceptions(TooManyUpdatesException.class).build();
	}

	@Bean
	public SsmAsyncClient ssmClient() {
		return SsmAsyncClient.builder().httpClient(httpClient()).build();
	}
	
	@Bean
	public SdkAsyncHttpClient httpClient() {
		return NettyNioAsyncHttpClient.create();
	}

	@Bean
	public ParameterStoreUtils ssmUtils(StsCallerIdentity identity) {
		return new ParameterStoreUtils(StringUtils.length(identity.getArn()));
	}
	
}
