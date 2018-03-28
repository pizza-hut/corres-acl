package org.acmepong.corres.acl.service;

//import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.acmepong.corres.acl.model.CorresJobRequest;
import org.acmepong.corres.acl.model.CorresJobResponse;
import org.acmepong.corres.acl.model.CorresStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Service
public class CorresServiceImpl implements ICorresService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${tango.base.url}")
	private String BASE_URL;
	
	@Value("${tango.ticket.url}")
	private String TICKET_URL;	
	
	@Value("${tango.job.url}")
	private String JOB_URL;
	
	@Value("${tango.username}")
	private String tangoUsername;
	
	@Value("${tango.password}")
	private String tangoPassword;
			
	@Override
	@Bean(name="submitJob")
	public CorresJobResponse submitJob(CorresJobRequest req) throws Exception {
		
		CorresJobResponse corresJobResponse = new CorresJobResponse();
				
		String ticket = getTangoTicket();
		logger.debug(ticket);
		Response res = this.submitTangoProduction(ticket, req);
		
		String submitResultXML = res.readEntity(String.class);
		logger.debug("submitJob readEntity :" + submitResultXML);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(new ByteArrayInputStream(submitResultXML.getBytes()));
		corresJobResponse.setJobId(document.getChildNodes().item(0).getAttributes().item(0).getTextContent());
		corresJobResponse.setPriority(document.getChildNodes().item(0).getChildNodes().item(0).getTextContent());
		corresJobResponse.setStatus(document.getChildNodes().item(0).getChildNodes().item(1).getTextContent());
		
		corresJobResponse.setResponseXML(submitResultXML);
		return corresJobResponse;
	}

	@Override
	@Bean(name="getStatus")
	public CorresStatusResponse getStatus(String siteId, String jobId) throws Exception {
		
		String ticket = this.getTangoTicket();
		return this.getTangoJobStatus(ticket, siteId, jobId);		
	}
	
	
	private String getTangoTicket() throws Exception {
		logger.debug(this.getClass() + "..." + "getTangoTicket");
		logger.debug(this.BASE_URL);
		
		Response res = null;
		String ticket = null;
		MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
		formParams.add("username", this.tangoUsername);
		formParams.add("password", this.tangoPassword);
		logger.debug(formParams.toString());
				
		Client client = ClientBuilder.newClient();
		
		WebTarget resource = client.target(this.BASE_URL);

		if (client!=null) {
			logger.debug(client.getConfiguration().toString());
		}  
		
		if (resource!=null) {
			logger.debug(resource.getClass() + ":" + resource.getUri().getPath().toString());
		}
						
		res = resource.path(this.TICKET_URL).request().accept(MediaType.TEXT_XML).post(Entity.form(formParams));
		
		if (res==null) {
			logger.debug("response is null");
		}
		
		if(res.getStatus() == 201) {
			logger.debug(res.getEntity().toString());
			String ticketResultXML = res.readEntity(String.class);
			logger.debug(ticketResultXML);
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(ticketResultXML.getBytes()));
			NodeList nodeList = document.getChildNodes();
			
			if (nodeList != null && nodeList.getLength() > 0) {
				ticket = nodeList.item(0).getFirstChild().getNodeValue();
			}			
		} else {
			logger.debug(res.getEntity().toString());
		}
		
		return ticket;		
	}
	
	private Response submitTangoProduction(String ticket, CorresJobRequest req) {
		
		Client client = ClientBuilder.newClient();
		
		MultivaluedHashMap<String, String> formParams = new MultivaluedHashMap<>();
		formParams.add("tango-ticket", ticket);
		formParams.add("project-name", req.getProjectName());
		formParams.add("data-file-contents", req.getDataFileContent());
		formParams.add("client-app", "CUI");		
		//formParams.add("effective-date", "2018/01/31");
		formParams.add("site-id", req.getSiteId());
		formParams.add("search-path", req.getSearchPaths());
		formParams.add("repoPath", req.getRepoPath());
				
		logger.debug("submitTangoProduction parameters:" + formParams.toString());
		WebTarget resource = client.target(BASE_URL);
		Response res = resource.path(this.JOB_URL).request().post(Entity.form(formParams));
		logger.debug(resource.getUri().getPath().toString());
		if (res.getStatus() == 201) {
			logger.debug("Job is submitted successfully");
			logger.debug(res.readEntity(String.class));			
		}		
		return res;
		
	}
	
	private CorresStatusResponse getTangoJobStatus(String ticket, String siteId, String jobId) throws Exception {		
		
		logger.debug(this.BASE_URL);
		logger.debug(this.JOB_URL);
		logger.debug(siteId);
		logger.debug(jobId);
		String url = this.BASE_URL + this.JOB_URL + jobId+"?"+"tango-ticket="+ticket+"&site-id="+siteId;
		logger.debug(url);
		
		logger.debug("Start RestTemplate...");
		
		RestTemplate restTemplate = new RestTemplate();
		CorresStatusResponse corresStatusResponse = restTemplate.getForObject(url, CorresStatusResponse.class);
		logger.debug(corresStatusResponse.toString());
		
		logger.debug("End RestTemplate");
						
		return corresStatusResponse;
	}
	
	public String getBASE_URL() {
		return this.BASE_URL;
	}
	
	public void setBASE_URL(String BASE_URL) {
		this.BASE_URL = BASE_URL;
	}
	
	public String getTICKET_URL() {
		return this.TICKET_URL;
	}
	
	public void setTICKET_URL(String TICKET_URL) {
		this.TICKET_URL = TICKET_URL;		
	}
	
	public String getJOB_URL() {
		return this.JOB_URL;
	}
	
	public void setJOB_URL(String JOB_URL) {
		this.JOB_URL = JOB_URL;
	}
	
	public String getTangoUsername() {
		return this.tangoUsername;
	}
	
	public void setTangoUsername(String tangoUsername) {
		this.tangoUsername = tangoUsername;
	}
	
	public String getTangoPassword() {
		return this.tangoPassword;
	}
	
	public void setTangoPassword(String tangoPassword) {
		this.tangoPassword = tangoPassword;
	}

}
