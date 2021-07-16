package org.egov.integration;

import java.util.ArrayList;

import org.egov.integration.model.Column;
import org.egov.integration.model.Columns;
import org.egov.integration.model.DisplayColumns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Slf4j
public class PreApplicationRunnerImpl implements ApplicationRunner {


	@Value("${egov.disp-columns.json.path}")
	private String displayColumnJSONPath;

	

	@Autowired
	private ResourceLoader resourceLoader;

	public static final Logger logger = LoggerFactory.getLogger(PreApplicationRunnerImpl.class);
	public static ArrayList<DisplayColumns> displayColumns = new ArrayList<>();
	

	@Override
	public void run(final ApplicationArguments arg0) throws Exception {
		try {
			logger.info("Reading JSON for display Column files......");
			readFiles();
		
		} catch (Exception e) {
			logger.error("Exception while loading JSON for display Column files: ", e);
		}
	}

	
	public void readFiles() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

		Columns columns = null;
		DisplayColumns displayCol = null;
	

		try {
			logger.info("Reading....: {}" ,displayColumnJSONPath);
			Resource resource = resourceLoader.getResource(displayColumnJSONPath);
				columns = mapper.readValue(resource.getInputStream(), Columns.class);

				if (columns != null) {
					for (Column col : (columns.getColumnMaps().getColumnConfig())) {
					
								displayCol = DisplayColumns.builder().build();							
								displayCol.setApplicationType(col.getApplicationType());
								displayCol.setEndPoint(col.getEndPoint());
								displayCol.setParameter1(col.getParameter1());
								displayCol.setParameter1Format(col.getParameter1Format());
								displayCol.setParameter2(col.getParametr2());
								displayCol.setParameter2Format(col.getParameter2Format());
								displayColumns.add(displayCol);
							
					}
									}
			logger.info("Parsed:{} ",displayColumns);
		} catch (Exception e) {
			logger.error("Exception while loading yaml files: ", e);
		}
	}

	public static ArrayList<DisplayColumns> getDisplayColumns() {
		return displayColumns;
	}

	

	public static DisplayColumns getSqlQuery( String applicationType) {
		String sqlQuery = "";
		DisplayColumns temp = displayColumns
				.stream().filter(value -> value.getApplicationType().equals(applicationType))
				.findFirst().orElse(null);
		
		return temp;
	}

	

}
