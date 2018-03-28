package org.acmepong.corres.acl.service;

import org.acmepong.corres.acl.model.CorresJobRequest;
import org.acmepong.corres.acl.model.CorresJobResponse;
import org.acmepong.corres.acl.model.CorresStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("Prototype")
public class CorresServiceHelper {

	ICorresService corresService;
	
	@Autowired
	public CorresJobResponse submitJob(CorresJobRequest req) throws Exception {
		return corresService.submitJob(req);		
	}
	
	@Autowired
	public CorresStatusResponse getStatus(String siteId, String jobId) throws Exception {
		return corresService.getStatus(siteId, jobId);
	}
	
	@Autowired
	public void setCorresApiService(ICorresService corresService) {
		this.corresService = corresService;
	}
}
