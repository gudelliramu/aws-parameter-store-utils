package org.learn.aws.ssm;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.learn.aws.ssm.service.ParameterStoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleTest {
	public static final Logger logger = LoggerFactory.getLogger(SampleTest.class);
	public static void main(String[] args) {
		logger.info("Sample");
		List<String> list = IntStream.range(1, 100).mapToObj(in -> {
			return "A Very Long String created to test split function" + in;
		}).collect(Collectors.toList());

		System.out.println("List: " + new ParameterStoreUtils(50).partitionList(list, 10, 60));
	}
}
