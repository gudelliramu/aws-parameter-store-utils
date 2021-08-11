package org.learn.aws.ssm.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterStringFilter;

public interface SsmService {
	
	int MAX_NUMBER_OF_TAGS_IN_FILTER = 50;
	int MAX_NUMBER_OF_VALUES_PER_TAG = 20;
	String TAG_FILTER_PREFIX = "tag:";
	String PATH_PREFIX = "/";

	Parameter getParameter(String parameterName);

	Parameter getParameter(String parameterName, boolean withDecryption);

	/**
	 * <p>
	 * Get list of parameters from SSM using the parameterNames. The returned
	 * parameters are without decryption.
	 * </p>
	 * 
	 * @param parameterNames Parameter names to get from SSM
	 * @return List of parameters matching the parameter names.
	 */
	List<Parameter> getParameters(Collection<String> parameterNames);

	/**
	 * <p>
	 * Get list of parameters from SSM using the parameterNames. The returned
	 * parameters are without decryption.
	 * </p>
	 * 
	 * @param parameterNames Parameter names to get from SSM
	 * @param withDecryption Whether to decrypt the parameter value. (Applied for
	 *                       SecureString)
	 * @return
	 */
	List<Parameter> getParameters(Collection<String> parameterNames, boolean withDecryption);

	List<ParameterStringFilter> createParameterStringFilters(Map<String, List<String>> tagFilters);

	@NonNull
	List<@NonNull Parameter> getParameterByPath(String path, boolean withDecryption);

	List<@NonNull Parameter> getParametersByPath(List<String> paths, boolean withDecryption);

}
