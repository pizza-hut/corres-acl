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
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

//@Component
@PropertySource("classpath:application.properties")
public class TangoServiceImpl implements ICorresService {
	
	private String BASE_URL;
	private String TICKET_URL;	
	private String JOB_URL;
	private String tangoUsername;
	private String tangoPassword;
	
	/*
	@Autowired
	private IProjectModelRepository projectModelRepository;
	*/
			
	@Override
	@Bean(name="submitJob")
	public CorresJobResponse submitJob(CorresJobRequest req) throws Exception {
		
		CorresJobResponse corresJobResponse = new CorresJobResponse();
		
		System.out.println(this.getClass() + "..." + "submitJob");
		String ticket = getTangoTicket();
		System.out.println(ticket);
		Response res = this.submitTangoProduction(ticket, req);
		
		String submitResultXML = res.readEntity(String.class);
		System.out.println("submitJob readEntity :" + submitResultXML);
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
		System.out.println(this.getClass() + "..." + "getTangoTicket");
		System.out.println(this.BASE_URL);
		
		Response res = null;
		String ticket = null;
		MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
		formParams.add("username", this.tangoUsername);
		formParams.add("password", this.tangoPassword);
		System.out.println(formParams.toString());
				
		Client client = ClientBuilder.newClient();
		
		WebTarget resource = client.target(this.BASE_URL);

		if (client!=null) {
			System.out.println(client.getConfiguration().toString());
		}  
		
		if (resource!=null) {
			System.out.println(resource.getClass() + ":" + resource.getUri().getPath().toString());
		}
						
		res = resource.path(this.TICKET_URL).request().accept(MediaType.TEXT_XML).post(Entity.form(formParams));
		
		if (res==null) {
			System.out.println("response is null");
		}
		
		if(res.getStatus() == 201) {
			System.out.println(res.getEntity().toString());
			String ticketResultXML = res.readEntity(String.class);
			System.out.println(ticketResultXML);
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(ticketResultXML.getBytes()));
			NodeList nodeList = document.getChildNodes();
			
			if (nodeList != null && nodeList.getLength() > 0) {
				ticket = nodeList.item(0).getFirstChild().getNodeValue();
			}			
		} else {
			System.out.println(res.getEntity().toString());
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
		
		//GET RepoPath from Redis here..KEY is repoPath, id is Project-Name
		//if (projectModelRepository == null) {
		//	System.out.println("---- check redis -------");
		//	System.out.println(projectModelRepository.toString());
		//}
				
		//ProjectModel project = projectModelRepository.get(req.getProjectName());		
		//System.out.println(project.getRepoPath());
		//formParams.add("repoPath", project.getRepoPath());
		
		
		System.out.println("submitTangoProduction parameters:" + formParams.toString());
		WebTarget resource = client.target(BASE_URL);
		Response res = resource.path(this.JOB_URL).request().post(Entity.form(formParams));
		System.out.println(resource.getUri().getPath().toString());
		if (res.getStatus() == 201) {
			System.out.println("Job is submitted successfully");
			System.out.println(res.readEntity(String.class));			
		}		
		return res;
		
	}
	
	private CorresStatusResponse getTangoJobStatus(String ticket, String siteId, String jobId) throws Exception {		
		
		System.out.println(this.BASE_URL);
		System.out.println(this.JOB_URL);
		System.out.println(siteId);
		System.out.println(jobId);
		String url = this.BASE_URL + this.JOB_URL + jobId+"?"+"tango-ticket="+ticket+"&site-id="+siteId;
		System.out.println(url);
		
		System.out.println("Start RestTemplate...");
		
		RestTemplate restTemplate = new RestTemplate();
		CorresStatusResponse corresStatusResponse = restTemplate.getForObject(url, CorresStatusResponse.class);
		System.out.println(corresStatusResponse.toString());
		
		System.out.println("End RestTemplate");
		
		/*
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpRequest = new HttpGet(url);
		HttpResponse httpResponse = httpClient.execute(httpRequest);
		System.out.println(httpResponse.getEntity().getContent().toString());
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
		    result.append(line);
		}
		
		JSONObject jsonObj = new JSONObject(result.toString());
		*/	
				
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
