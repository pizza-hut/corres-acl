package org.acmepong.corres.acl.service;

import org.acmepong.corres.acl.model.CorresJobRequest;
import org.acmepong.corres.acl.model.CorresJobResponse;
import org.acmepong.corres.acl.model.CorresStatusResponse;

//import javax.ws.rs.core.Response;

public interface ICorresService {
	
	CorresJobResponse submitJob(CorresJobRequest req) throws Exception;
	
	CorresStatusResponse getStatus(String siteId, String jobId) throws Exception;
	
}
