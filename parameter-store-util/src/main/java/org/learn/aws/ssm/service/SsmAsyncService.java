package org.learn.aws.ssm.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterMetadata;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter.Builder;
import software.amazon.awssdk.services.ssm.paginators.DescribeParametersPublisher;
import software.amazon.awssdk.services.ssm.paginators.GetParametersByPathPublisher;

@Service
public class SsmAsyncService implements SsmService {

	private static final Logger logger = LoggerFactory.getLogger(SsmAsyncService.class);

	private SsmAsyncClient client;

	private ParameterStoreUtils utils;

	@Autowired
	public SsmAsyncService(SsmAsyncClient client, ParameterStoreUtils utils) {
		this.client = client;
		this.utils = utils;
	}

	@Override
	public List<Parameter> getParameters(Collection<String> parameterNames) {
		return getParameters(parameterNames, false);
	}

	@Override
	public List<Parameter> getParameters(Collection<String> parameterNames, boolean withDecryption) {
		List<List<String>> partitionList = utils.partitionLList(parameterNames);

		List<CompletableFuture<GetParametersResponse>> futures = new ArrayList<>();

		partitionList.forEach((partionedNames) -> {
			futures.add(client.getParameters((builder) -> builder.names(partionedNames).withDecryption(Boolean.TRUE)));
		});

		List<Parameter> parametersList = futures.stream().map(CompletableFuture::join)
				.flatMap(getParamResp -> getParamResp.parameters().stream()).collect(Collectors.toList());

		logger.info("Parameter size: {}", parametersList.size());
		return parametersList;
	}

	@Override
	public Parameter getParameter(String parameterName) {
		return getParameter(parameterName, false);
	}

	@Override
	public Parameter getParameter(String parameterName, boolean withDecryption) {
		CompletableFuture<GetParameterResponse> parameterFuture = client
				.getParameter(builder -> builder.name(parameterName).withDecryption(withDecryption));
		return parameterFuture.join().parameter();
	}

	public List<Parameter> getParametersByTags(Map<String, List<String>> tagsFilters) {
		List<String> parameterNames = getParametersMetadata(tagsFilters).stream().map(ParameterMetadata::name)
				.collect(Collectors.toList());
		return getParameters(parameterNames);
	}

	@Override
	public List<@NonNull Parameter> getParametersByPath(List<String> paths, final boolean withDecryption) {
		return paths.stream().flatMap(path -> getParameterByPath(path, withDecryption).stream())
				.collect(Collectors.toList());
	}

	@Override
	public @NonNull List<@NonNull Parameter> getParameterByPath(@NonNull String path, boolean withDecryption) {
		Preconditions.checkNotNull(path, "path cannot be null.");
		final String pathWithPrefix = StringUtils.appendIfMissing(path, PATH_PREFIX, PATH_PREFIX);

		GetParametersByPathPublisher paginator = client.getParametersByPathPaginator(builder -> {
			builder.path(pathWithPrefix).withDecryption(Boolean.valueOf(withDecryption));
		});

		return Flowable.fromPublisher(paginator).flatMapIterable(mapper -> mapper.parameters()).toList().blockingGet();
	}

	public @NonNull List<@NonNull ParameterMetadata> getParametersMetadata(Map<String, List<String>> tagsFilters) {

		DescribeParametersPublisher publisher = client.describeParametersPaginator(builder -> {
			builder.parameterFilters(createParameterStringFilters(tagsFilters));
		});

		return Flowable.fromPublisher(publisher).flatMapIterable(mapper -> mapper.parameters()).toList().blockingGet();
	}

	public void deleteParametersByPath(@NonNull List<String> paths, Optional<Map<String, List<String>>> tagFilters) {
		
//		ParameterStringFilter.builder().key(path)
		
//		client.describeParametersPaginator(builder -> {builder.filters(filterBuilder -> {filterBuilder.key(ParametersFilterKey.)}; )};);
	}

	@Override
	public List<ParameterStringFilter> createParameterStringFilters(Map<String, List<String>> tagFilters) {
		Preconditions.checkNotNull(tagFilters, "tagFilters must not be null");

		Preconditions.checkArgument(tagFilters.size() <= MAX_NUMBER_OF_TAGS_IN_FILTER,
				"Number of tags can not exceed '%s' but found '%s'", MAX_NUMBER_OF_TAGS_IN_FILTER, tagFilters.size());

		List<ParameterStringFilter> filters = new ArrayList<>();
		tagFilters.forEach((key, val) -> {
			if (key == null) {
				return; // Skips null key items only.
			}
			Builder filterBuilder = ParameterStringFilter.builder().key(TAG_FILTER_PREFIX + key);
			if (val != null) {
				Preconditions.checkState(val.size() <= MAX_NUMBER_OF_VALUES_PER_TAG,
						"Number of values per key cannot exceed '%s' but key '%s' has '%s' values. ",
						MAX_NUMBER_OF_VALUES_PER_TAG, key, val.size());
			}
			filterBuilder.values(val.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));

			filters.add(filterBuilder.build());
		});

		return filters;
	}
}
