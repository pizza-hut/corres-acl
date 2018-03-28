package org.acmepong.corres.acl.repository;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.acmepong.corres.acl.model.ProjectModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRepoImpl implements IRedisRepo {

	private static final String KEY = "Project";

	private RedisTemplate<String, Object> redisTemplate;
	private HashOperations<String, String, ProjectModel> hashOperations;

	@Autowired
	public RedisRepoImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@PostConstruct
	private void init() {
		hashOperations = redisTemplate.opsForHash();
	}

	@Override
	public void save(ProjectModel projectModel) {
		hashOperations.put(KEY, projectModel.getProjectName(), projectModel);
	}
		
	@Override
	public ProjectModel find(String projectName) {
		return hashOperations.get(KEY, projectName);
	}

	@Override
	public Map<String, ProjectModel> findAll() {
		//return null;
		return hashOperations.entries(KEY);
	}

	@Override
	public void update(ProjectModel projectModel) {
		hashOperations.put(KEY, projectModel.getProjectName(), projectModel);
	}

	@Override
	public void delete(String projectName) {
		hashOperations.delete(KEY, projectName);
	}

}
