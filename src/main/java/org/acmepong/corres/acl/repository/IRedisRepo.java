package org.acmepong.corres.acl.repository;

import java.util.Map;

import org.acmepong.corres.acl.model.ProjectModel;

public interface IRedisRepo {
	void save(ProjectModel projectModel);	
	ProjectModel find(String projectName);
	Map<String, ProjectModel> findAll();
	void update(ProjectModel projectModel);
	void delete(String projectName);
}
