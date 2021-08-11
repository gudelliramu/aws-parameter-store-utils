package org.learn.aws.sts;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.services.sts.StsAsyncClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

@Configuration
public class StsConfiguration {

	@Autowired
	private SdkAsyncHttpClient httpClient;

	@Bean
	public StsAsyncClient stsClient() {
		return StsAsyncClient.builder().httpClient(httpClient).build();
	}

	@Bean
	public StsCallerIdentity callerIdentity() {
		CompletableFuture<GetCallerIdentityResponse> responseFuture = stsClient().getCallerIdentity();
		CompletableFuture<StsCallerIdentity> identityFuture = responseFuture.thenApply(resp -> {
			return StsCallerIdentity.builder().account(resp.account()).userId(resp.userId()).arn(resp.arn()).build();
		});

		StsCallerIdentity identity = identityFuture.join();
		return identity;
	}

}
