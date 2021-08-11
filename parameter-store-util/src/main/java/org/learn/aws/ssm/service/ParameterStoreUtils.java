package org.learn.aws.ssm.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import com.google.common.base.Preconditions;

public class ParameterStoreUtils {

	private static final Logger logger = LoggerFactory.getLogger(ParameterStoreUtils.class);

	private static final int DEFAULT_PARTITION_SIZE = 10;
	private static final int DEFAULT_MAX_STRING_SIZE = 1011;
	private final int maxParameterNameLength;

	public ParameterStoreUtils(int arnLength) {
		this.maxParameterNameLength = DEFAULT_MAX_STRING_SIZE - arnLength;
	}

	/**
	 * Splits the given strList, this method partitions the list by either
	 * {@link #DEFAULT_PARTITION_SIZE} or by cumulative string length of
	 * {@link #DEFAULT_MAX_STRING_SIZE}
	 * 
	 * @param strList
	 * @return List of Partitioned strings.
	 */
	public List<List<String>> partitionLList(@NonNull Collection<? extends CharSequence> strList) {
		return partitionList(strList, DEFAULT_PARTITION_SIZE, this.maxParameterNameLength);
	}

	/**
	 * 
	 * @param strList
	 * @param partitionSize
	 * @param maxStringSize
	 * @return
	 */
	public List<List<String>> partitionList(@NonNull Collection<? extends CharSequence> strList,
			int partitionSize, int maxStringSize) {

		Objects.requireNonNull(strList, "strList must not be null.");
		final List<List<String>> partitionList = new ArrayList<>();
		int counter = 0;
		int stringLen = 0;
		List<String> partition = null;
		for (CharSequence item : strList) {

			if (counter == 0) {
				partition = new ArrayList<>();
			}
			if (StringUtils.isBlank(item)) {
				continue;
			}
			int currStrLen = StringUtils.length(item);
			Preconditions.checkState(currStrLen <= maxStringSize,
					"String '%s' is longer than maxStringSize '%s'. Cannot partition this list", item, maxStringSize);
			stringLen += currStrLen;

			if (counter < partitionSize && stringLen <= maxStringSize) {
				partition.add(item.toString());
			} else {
				counter = 0;
				stringLen = 0;
				partitionList.add(partition);
				partition = new ArrayList<>();
				partition.add(item.toString());
			}
			counter++;

		}
		if (counter > 0) {
			partitionList.add(partition);
		}

		return partitionList;
	}
}
