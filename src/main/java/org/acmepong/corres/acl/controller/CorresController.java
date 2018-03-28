package org.acmepong.corres.acl.controller;

import org.acmepong.corres.acl.model.CorresJobRequest;
import org.acmepong.corres.acl.model.CorresJobResponse;
import org.acmepong.corres.acl.model.CorresStatusResponse;
import org.acmepong.corres.acl.model.ProjectModel;
import org.acmepong.corres.acl.repository.IRedisRepo;
import org.acmepong.corres.acl.service.CorresServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CorresController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CorresServiceImpl corresService;
	
	
	@Autowired
	private IRedisRepo redisRepo;
	
	@RequestMapping("/corres/acl/save")
	public String save() {
		// save a single Project
		redisRepo.save(new ProjectModel("ShippingOrder", "01Exercise\\ShippingOrder"));
		return "Added successfully";
	}
	
	@PostMapping(value="/corres/acl/submitJob")	
	public CorresJobResponse submitJob(@RequestBody CorresJobRequest req) {
		
		CorresJobResponse corresJobResponse = null;
		
		logger.debug("-----------------------------------------------------");
		logger.debug("--------Received request-----------------------------");
		logger.debug(req.getProjectName());		
		logger.debug(req.getSiteId());
		logger.debug(req.getSearchPaths());
		logger.debug(req.getDataFileContent());		
		logger.debug("-----------------------------------------------------");
		
		
		if (redisRepo!= null) {
			logger.debug("------ reading from repo -----");
			ProjectModel model = redisRepo.find("ShippingOrder");
			if (model==null) {
				logger.debug("no data found in repo");
			} else {
				logger.debug(model.getClass() + " " + model.toString());
				req.setRepoPath(model.getRepoPath());
			}
		}
		
		
		try {
			corresJobResponse = corresService.submitJob(req);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		logger.debug("--------Return Response-----------------------------");
		logger.debug(corresJobResponse.getResponseXML());
		logger.debug("-----------------------------------------------------");
		return corresJobResponse;				
	}
	
	@RequestMapping(value="/corres/acl/{siteId}/{jobId}", method=RequestMethod.GET, produces="application/json")
	public CorresStatusResponse getStatus(@PathVariable String siteId, @PathVariable String jobId) {
		
		CorresStatusResponse corresStatusResponse = new CorresStatusResponse();
				
		try {
			corresStatusResponse = corresService.getStatus(siteId, jobId);		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		
		return corresStatusResponse;
	}

}
