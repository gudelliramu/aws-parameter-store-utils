package org.learn.aws.ssm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.learn.aws.ssm.service.ParameterStoreUtils;
import org.learn.aws.ssm.service.SsmAsyncService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.common.collect.ImmutableMap;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;
import software.amazon.awssdk.services.ssm.model.TooManyUpdatesException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ParameterStoreUtilApplicationTests {

	private static final String CSP_PARAMETER_NAME_PREFIX = "/csp/parameter";

	@Autowired
	private RetryRegistry registry;

	@Autowired
	private RetryConfig config;

	@Mock
	private SsmAsyncClient client;

	@Autowired
	private SsmAsyncService ssmService;

	@Test
	public void when_TooManyExceptions_thrown_then_retry() {
		assertNotNull(registry);
		assertNotNull(config);
		when(client.putParameter(any(PutParameterRequest.class))).thenThrow(TooManyUpdatesException.class)
				.thenReturn(CompletableFuture.completedFuture(PutParameterResponse.builder().build()));

		registry.retry("temp", config).executeSupplier(() -> {
			return client.putParameter(PutParameterRequest.builder().build());
		});

		verify(client, times(2)).putParameter(PutParameterRequest.builder().build());

	}

	@Test
	public void split_string_list() {

		List<String> list = IntStream.range(1, 100).mapToObj(in -> {
			return "A Very Long String created to test split function" + in;
		}).collect(Collectors.toList());

		System.out.println("List: " + new ParameterStoreUtils(50).partitionList(list, 10, 400));
	}

	@Test
	public void test_get_parameters() {
		final int parameter_count = 12;
		String paramName = "/csp/parameter";
		Set<String> parameterNames = IntStream.rangeClosed(1, parameter_count).mapToObj(i -> paramName + i)
				.collect(Collectors.toSet());

		List<Parameter> paramList = ssmService.getParameters(parameterNames, true);

		assertEquals(parameter_count, paramList.size());
	}

	@Test
	public void when_key_is_null_exclude_parameter_filter() {
		Map<String, List<String>> filters = new HashMap<>();
		filters.put(null, Lists.<String>emptyList());
		filters.put("dummy", Lists.list("Value1", "Value2"));
		List<ParameterStringFilter> filterList = ssmService.createParameterStringFilters(filters);
		assertEquals(1, filterList.size());
	}

	@Test
	public void get_parameters_by_names() {
		List<Parameter> parameters = ssmService
				.getParameters(Arrays.asList(CSP_PARAMETER_NAME_PREFIX + "900", CSP_PARAMETER_NAME_PREFIX + "901"));
		assertEquals(2, parameters.size());
	}

	@Test
	public void get_parameters_by_name() {
		Parameter parameters = ssmService.getParameter(CSP_PARAMETER_NAME_PREFIX + "900");
		assertNotNull(parameters, "Returned parameter should not null");
		assertNotNull(parameters.value(), "Parameter value should not be null");
	}

	@Test
	public void get_parameters_by_tags() {
		Map<String, List<String>> filters = new HashMap<>();
		filters.put("user", Arrays.asList("gudelli", "rgudelli"));
		filters.put("source", Arrays.asList("junit"));
		List<Parameter> parameters = ssmService.getParametersByTags(filters);
		assertEquals(1, parameters.size());
	}

	@Test
	public void when_filters_is_null_throws_nullpointerexception() {
		assertThrows(NullPointerException.class, () -> {
			ssmService.getParametersByTags(null);
		});
	}

	@Test
	public void when_number_of_filter_tag_vales_greaterthan_allowed_throw_illegal_state_exception() {
		int maxValuesPerTag = SsmAsyncService.MAX_NUMBER_OF_VALUES_PER_TAG;
		List<String> values = IntStream.rangeClosed(0, maxValuesPerTag).mapToObj(i -> "" + i)
				.collect(Collectors.toList());
		assertThrows(IllegalStateException.class, () -> {
			ssmService.getParametersByTags(ImmutableMap.<String, List<String>>of("user", values));
		});
	}

	@Test
	public void when_number_of_filter_tag_keys_greaterthan_allowed_throw_illegal_state_exception() {
		int maxTagsPerFilter = SsmAsyncService.MAX_NUMBER_OF_TAGS_IN_FILTER;
		List<String> values = IntStream.rangeClosed(0, maxTagsPerFilter).mapToObj(i -> "" + i)
				.collect(Collectors.toList());
		assertThrows(IllegalStateException.class, () -> {
			ssmService.getParametersByTags(ImmutableMap.<String, List<String>>of("user", values));
		});
	}

	@Test
	public void get_parameters_by_path() {
		List<String> paths = Arrays.asList("/csp/parameter");
		List<Parameter> parameters = ssmService.getParametersByPath(paths, true);
		assertEquals(1, parameters.size());

	}
	
	@Test
	public void get_parameter_by_name() {
		Parameter parameter = ssmService.getParameter("/csp/parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456parameter111456211111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111", true);
		assertNotNull(parameter,"");

	}
	
	
	

}
