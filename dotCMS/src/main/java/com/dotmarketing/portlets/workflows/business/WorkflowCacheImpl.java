package com.dotmarketing.portlets.workflows.business;

import com.dotcms.util.CollectionsUtils;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotCacheAdministrator;
import com.dotmarketing.business.DotCacheException;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.workflows.model.*;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.google.common.collect.ImmutableList;
import com.liferay.util.StringPool;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//This interface should have default package access
public class WorkflowCacheImpl extends WorkflowCache {

	private DotCacheAdministrator cache;
	final static String FOUR_OH_FOUR_TASK = "404WorkflowTask";
	public WorkflowCacheImpl() {
		cache = CacheLocator.getCacheAdministrator();
	}

	protected WorkflowScheme addDefaultScheme(WorkflowScheme scheme) {

		cache.put(defaultKey, scheme, getPrimaryGroup());
		return scheme;
	}

	protected List<WorkflowScheme> addForStructure(String struct, List<WorkflowScheme> schemes) {
		String key = struct;

		// Add the key to the cache
		cache.put(key, schemes, getPrimaryGroup());
		return schemes;
		
	}
	
	protected WorkflowScheme add(WorkflowScheme scheme) {

		String key = scheme.getId();

		// Add the key to the cache
		cache.put(key, scheme, getPrimaryGroup());

			return scheme;
		
	}

	public WorkflowScheme getScheme(String key) {

		WorkflowScheme scheme = null;
		try {
			scheme = (WorkflowScheme) cache.get(key, getPrimaryGroup());
		} catch (DotCacheException e) {
			Logger.debug(this, "Cache Entry not found", e);
		}
		return scheme;
	}

	
	protected List<WorkflowScheme> getSchemesByStruct(String structId) {


		try {
			return  (List<WorkflowScheme>) cache.get(structId, getPrimaryGroup());
		} catch (DotCacheException e) {
			Logger.debug(this, "Cache Entry not found", e);
		}
		return null;
	}
	
	protected void removeStructure(String struct) {
		if (struct != null ) {
			cache.remove(struct, getPrimaryGroup());
		}
	}
	
	
	public void remove(WorkflowScheme scheme) {
		if (scheme != null && UtilMethods.isSet(scheme)) {
			cache.remove(scheme.getId(), getPrimaryGroup());
		}
	}

	public void remove(WorkflowStep step) {
		cache.remove(step.getId(), STEP_GROUP);
		cache.remove(step.getId(), ACTION_GROUP);
	}
	
	protected void removeActions(WorkflowStep step) {
		if(step != null)
			cache.remove(step.getId(), ACTION_GROUP);
	}

	protected void removeActions(final WorkflowScheme scheme) {
		if(scheme != null)
			cache.remove(scheme.getId(), ACTION_GROUP);
	}
	
	protected void remove(WorkflowTask task) {
		if (UtilMethods.isSet(task) && UtilMethods.isSet(task.getWebasset())) {
			cache.remove(this.getKey(task.getWebasset(), task.getLanguageId()), TASK_GROUP); // remove the task id.
			cache.remove(this.getKey(task.getWebasset(), task.getLanguageId()), STEP_GROUP);
		}
		if (UtilMethods.isSet(task) && UtilMethods.isSet(task.getId())) {
			cache.remove(task.getId(), TASK_GROUP); // remove the task.
		}
	}
	
	protected void remove(final Contentlet contentlet) {

		if (contentlet != null && UtilMethods.isSet(contentlet.getIdentifier())) {

			cache.remove(this.getKey(contentlet),   STEP_GROUP);
			cache.remove(this.getKey(contentlet), TASK_GROUP);
		}
	}
	public WorkflowScheme getDefaultScheme() {
		return getScheme(defaultKey);
	}

	public void clearCache() {
		// clear the cache
		for(String x : getGroups()){
			cache.flushGroup(x);
		}
	}
	@Override
	protected void clearStepsCache() {
		// clear the cache
		cache.flushGroup(STEP_GROUP);
		
	}
	protected WorkflowStep getStep(String key) {
		WorkflowStep step = null;
		try {
			step = (WorkflowStep) cache.get(key, STEP_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(this, "Cache Entry not found", e);
		}
		return step;
	}

	protected WorkflowStep add(WorkflowStep step) {
		String key = step.getId();

		// Add the key to the cache
		cache.put(key, step, STEP_GROUP);

			return step;
		
	}

	protected boolean is404(final Contentlet contentlet) {

		String taskId = null;

		try {

			taskId = (String) cache.get(this.getKey(contentlet), TASK_GROUP);
		} catch (DotCacheException e) {
			Logger.error(this.getClass(), e.getMessage());
		}

		return (FOUR_OH_FOUR_TASK.equals(taskId));
	}
	
	 protected WorkflowTask getTask(final Contentlet contentlet){
		
		WorkflowTask task = null;
		try {

			final String taskId = (String) cache.get(this.getKey(contentlet), TASK_GROUP);
			task			    = (WorkflowTask) cache.get(taskId, 			  TASK_GROUP);
		} catch (Exception e) {
			Logger.debug(this.getClass(), e.getMessage());
		}
		return task;
	 }

	 protected List<WorkflowStep> getSteps(Contentlet contentlet){
			
		 List<WorkflowStep> steps = null;
			try {
				steps = (List<WorkflowStep>) cache.get(this.getKey(contentlet), STEP_GROUP);
			} catch (Exception e) {
				Logger.debug(this.getClass(), e.getMessage());
			}
			return steps;
	 }
	 
	protected WorkflowStep addStep(WorkflowStep step) {
		 if(step ==null || !UtilMethods.isSet(step.getId()) ){
			 return null;
		 }

		cache.put(step.getId(), step , STEP_GROUP);

		return step;
	}
	 
	protected WorkflowTask addTask(final WorkflowTask task) {

		 if(task ==null || !UtilMethods.isSet(task.getId()) ){
			 return null;
		 }

		cache.put(task.getId(), task , TASK_GROUP);		

		return task;
	}
	
	
	
	protected List<WorkflowStep> addSteps(Contentlet contentlet, List<WorkflowStep> steps) {
		 if(contentlet ==null || !UtilMethods.isSet(contentlet.getIdentifier())
				 || steps ==null ){
			 return null;
		 }


		// Add the Step id as a string to the cache
		cache.put(this.getKey(contentlet), steps, STEP_GROUP);
		return steps;


	}
	@Override
	protected void add404Task(final Contentlet contentlet) {
		 if(contentlet ==null || !UtilMethods.isSet(contentlet.getIdentifier())){
			 return;
		 }
		// Add the key to the cache
		cache.put(this.getKey(contentlet), FOUR_OH_FOUR_TASK, TASK_GROUP);
		return;
	}
	 
	protected WorkflowTask addTask(final Contentlet contentlet, final WorkflowTask task) {

		 if(contentlet ==null || !UtilMethods.isSet(contentlet.getIdentifier())
				 || task ==null || !UtilMethods.isSet(task.getId()) ) {

			 return null;
		 }

		// Add the key to the cache
		cache.put(this.getKey(contentlet),
				task.getId(), TASK_GROUP);
		return addTask(task);
	}

	private String getKey (final Contentlet contentlet) {

	 	return this.getKey(contentlet.getIdentifier(), contentlet.getLanguageId());
	}

	private String getKey (final String assetId, final long languageId) {

		return new StringBuilder(assetId)
				.append("_").append(languageId).toString();
	}

	@Override
	protected List<WorkflowAction> addActions(WorkflowStep step, List<WorkflowAction> actions) {
		if(step == null || actions==null)return null;
		cache.put(step.getId(), actions, ACTION_GROUP);
		return actions;
		
	}

	protected List<WorkflowAction> addActions(final WorkflowScheme scheme,
											  final List<WorkflowAction> actions) {

		if(scheme == null || actions==null)return null;

		cache.put(scheme.getId(), actions, ACTION_GROUP);
		return actions;
	}

	@Override
	protected List<WorkflowAction> getActions(WorkflowStep step) {
		if(step == null ) return null;
		try {
			return (List<WorkflowAction>) cache.get(step.getId(), ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(),e);
		}
		return null;
	}

	@Override
	protected List<WorkflowActionClass> getActionClasses(final WorkflowAction action) {

		if(action != null ) {
			try {
				return (List<WorkflowActionClass>) cache.get(action.getId(), ACTION_CLASS_GROUP);
			} catch (DotCacheException e) {
				Logger.debug(WorkflowCacheImpl.class, e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	protected List<WorkflowActionClass> addActionClasses(final WorkflowAction action,
													final List<WorkflowActionClass> actionClasses) {

		if(action == null || actionClasses==null) {
			return null;
		}

		cache.put(action.getId(), actionClasses, ACTION_CLASS_GROUP);
		return actionClasses;
	}

	@Override
	public void remove(final WorkflowAction action) {

		cache.remove(action.getId(), ACTION_CLASS_GROUP);
	}

	@Override
	protected List<WorkflowAction> getActions(WorkflowScheme scheme) {
		if(scheme == null ) return null;
		try {
			return (List<WorkflowAction>) cache.get(scheme.getId(), ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(),e);
		}
		return null;
	}

	private static final String SYSTEM_ACTION_KEY_MAIN = "MAIN";
	private static final String SYSTEM_ACTION_KEY_REFERENCES = "REFERENCES";

	/*
	The system action mapping cache is indexed by system action mapping id, but there are also other approaches to get the
	system action mappings, such as, by workflow id, content type variable, workflow scheme id, system action name + scheme id list, system action name + content type variable
	the most of them has an list of them, so when some of the is being removed, the references on the MAIN cache should be also removed. In viceversa, if some of the elements on
	the MAIN cache is being removed, the references to it are not longer valid, so they have to be removed too.
	The following method encapsulate the logic to remove and add MAIN objects and the reference lists
	 */

	@Override
	public Map<String, Object> findSystemActionByIdentifier(final String identifier) {

		if (UtilMethods.isSet(identifier)) {

			try {
				return (Map<String, Object>) cache.get(SYSTEM_ACTION_KEY_MAIN+identifier, SYSTEM_ACTION_GROUP);
			} catch (DotCacheException e) {
				Logger.debug(WorkflowCacheImpl.class,e.getMessage(),e);
			}
		}

		return null;
	}

	@Override
	public Map<String, Object> findSystemActionByContentType(final String systemActionName, final String variable) {

		Map<String, Object> mappingRow = null;

		try {
			final String systemMappingId = (String)this.cache.get(
					SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_CONTENTTYPE.name()
							+ StringUtils.join(new String[]{systemActionName, variable}, StringPool.COMMA), SYSTEM_ACTION_GROUP);

			if (UtilMethods.isSet(systemMappingId)) {

				mappingRow = this.findSystemActionByIdentifier(systemMappingId);

			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}

		return mappingRow;
	}

	@Override
	public List<Map<String, Object>> findSystemActionsByContentType(final String variable) {

		List<Map<String, Object>> mappingRows = null;

		try {
			final List<String> systemMappingIds = (List<String>)this.cache.get(
					SystemActionMappingReferenceCache.TYPE.CONTENTTYPE.name()+variable, SYSTEM_ACTION_GROUP);

			if (UtilMethods.isSet(systemMappingIds)) {

				mappingRows = new ArrayList<>();
				for (final String mappingId : systemMappingIds) {

					final Map<String, Object> row = this.findSystemActionByIdentifier(mappingId);
					mappingRows.add(row);
				}
			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}

		return mappingRows;
	}

	@Override
	public List<Map<String, Object>> findSystemActionsByScheme(final String schemeId) {

		List<Map<String, Object>> mappingRows = null;

		try {
			final List<String> systemMappingIds = (List<String>)this.cache.get(
					SystemActionMappingReferenceCache.TYPE.SCHEME.name()+schemeId, SYSTEM_ACTION_GROUP);

			if (UtilMethods.isSet(systemMappingIds)) {

				mappingRows = new ArrayList<>();
				for (final String mappingId : systemMappingIds) {

					final Map<String, Object> row = this.findSystemActionByIdentifier(mappingId);
					mappingRows.add(row);
				}
			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}

		return mappingRows;
	}

	@Override
	public List<Map<String, Object>> findSystemActionsByWorkflowAction(final String workflowActionId) {

		List<Map<String, Object>> mappingRows = null;

		try {
			final List<String> systemMappingIds = (List<String>)this.cache.get(
					SystemActionMappingReferenceCache.TYPE.WORKFLOW.name()+workflowActionId, SYSTEM_ACTION_GROUP);

			if (UtilMethods.isSet(systemMappingIds)) {

				mappingRows = new ArrayList<>();
				for (final String mappingId : systemMappingIds) {

					final Map<String, Object> row = this.findSystemActionByIdentifier(mappingId);
					mappingRows.add(row);
				}
			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}

		return mappingRows;
	}

	@Override
	public  List<Map<String, Object>> findSystemActionsBySchemes(final String systemActionName, final List<String> schemeIdList) {

		List<Map<String, Object>> mappingRows = null;

		try {
			final List<String> systemMappingIds = (List<String>)this.cache.get(
					SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_SCHEMES.name()
							+ CollectionsUtils.join(StringPool.COMMA, systemActionName, schemeIdList), SYSTEM_ACTION_GROUP);

			if (UtilMethods.isSet(systemMappingIds)) {

				mappingRows = new ArrayList<>();
				for (final String mappingId : systemMappingIds) {

					final Map<String, Object> row = this.findSystemActionByIdentifier(mappingId);
					mappingRows.add(row);
				}
			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}

		return mappingRows;
	}

	@Override
	public void addSystemActionByIdentifier(final String identifier, final Map<String, Object> systemActionMap) {

		if(UtilMethods.isSet(identifier) && UtilMethods.isSet(systemActionMap)) {

			cache.put(SYSTEM_ACTION_KEY_MAIN+identifier, systemActionMap, SYSTEM_ACTION_GROUP);
		}
	}

	@Override
	public void addSystemActionBySystemActionNameAndContentTypeVariable(final String systemActionName,
																		final String variable,
																		final Map<String, Object> mappingRow) {

		try {

			final String mappingIdentifier = (String)mappingRow.get("id");
			this.addSystemActionByIdentifier(mappingIdentifier, mappingRow);
			this.addSystemActionReference(mappingIdentifier, new SystemActionMappingReferenceCache.Builder()
					.systemActionByContentType(systemActionName, variable).build());

			this.cache.put(
			SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_CONTENTTYPE.name()
					+ StringUtils.join(new String[]{systemActionName, variable}, StringPool.COMMA), mappingIdentifier, SYSTEM_ACTION_GROUP);

		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	private void addSystemActionReference (final String mappingIdentifier, final SystemActionMappingReferenceCache referenceCache) throws DotCacheException {

		final String reverseReferenceKey = SYSTEM_ACTION_KEY_REFERENCES + mappingIdentifier;
		List<SystemActionMappingReferenceCache> referenceCaches =
				(List<SystemActionMappingReferenceCache>)this.cache.get(reverseReferenceKey, SYSTEM_ACTION_GROUP);
		if (!UtilMethods.isSet(referenceCaches)) {
			referenceCaches = new ArrayList<>();
		}
		referenceCaches.add(referenceCache);
		this.cache.put(reverseReferenceKey, referenceCaches, SYSTEM_ACTION_GROUP);
	}

	@Override
	public void addSystemActionsByWorkflowAction(final String workflowActionId, final List<Map<String, Object>> results) {

		try {

			final ImmutableList.Builder<String> systemActionMappingIdentifierListBuilder =
					new ImmutableList.Builder<>();

			// adding one by one
			for (final Map<String, Object> mappingRow : results) {

				final String mappingIdentifier = (String)mappingRow.get("id");
				this.addSystemActionByIdentifier(mappingIdentifier, mappingRow);

				// adding scheme reverse reference to the row
				this.addSystemActionReference(mappingIdentifier, new SystemActionMappingReferenceCache.Builder()
						.workflowId(workflowActionId).build());
			}

			// now the reference id list for this search criteria
			this.cache.put(
					SystemActionMappingReferenceCache.TYPE.WORKFLOW.name()	+ workflowActionId,
							systemActionMappingIdentifierListBuilder.build(), SYSTEM_ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void addSystemActionsByContentType(final String variable, final List<Map<String, Object>> results) {

		try {

			final ImmutableList.Builder<String> systemActionMappingIdentifierListBuilder =
					new ImmutableList.Builder<>();

			// adding one by one
			for (final Map<String, Object> mappingRow : results) {

				final String mappingIdentifier = (String)mappingRow.get("id");
				this.addSystemActionByIdentifier(mappingIdentifier, mappingRow);

				// adding scheme reverse reference to the row
				this.addSystemActionReference(mappingIdentifier, new SystemActionMappingReferenceCache.Builder()
						.contentTypeVar(variable).build());
			}

			// now the reference id list for this search criteria
			this.cache.put(
					SystemActionMappingReferenceCache.TYPE.CONTENTTYPE.name() + variable,
						 systemActionMappingIdentifierListBuilder.build(), SYSTEM_ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void addSystemActionsByScheme(final String schemeId, final List<Map<String, Object>> results) {

		try {

			final ImmutableList.Builder<String> systemActionMappingIdentifierListBuilder =
					new ImmutableList.Builder<>();

			// adding one by one
			for (final Map<String, Object> mappingRow : results) {

				final String mappingIdentifier = (String)mappingRow.get("id");
				this.addSystemActionByIdentifier(mappingIdentifier, mappingRow);

				// adding scheme reverse reference to the row
				this.addSystemActionReference(mappingIdentifier,
						new SystemActionMappingReferenceCache.Builder()
						.schemeId(schemeId).build());
			}

			// now the reference id list for this search criteria
			this.cache.put(
					SystemActionMappingReferenceCache.TYPE.SCHEME.name() + schemeId,
							systemActionMappingIdentifierListBuilder.build(), SYSTEM_ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void addSystemActionsBySystemActionNameAndSchemeIds(final String systemActionName,
															   final List<String> schemeIdList,
															   final List<Map<String, Object>> results) {

		try {

			final ImmutableList.Builder<String> systemActionMappingIdentifierListBuilder =
								new ImmutableList.Builder<>();

			// adding one by one
			for (final Map<String, Object> mappingRow : results) {

				final String mappingIdentifier = (String)mappingRow.get("id");
				this.addSystemActionByIdentifier(mappingIdentifier, mappingRow);

				// adding scheme reverse reference to the row
				this.addSystemActionReference(mappingIdentifier, new SystemActionMappingReferenceCache.Builder()
						.systemActionBySchemeIds(systemActionName, schemeIdList).build());
			}

			// now the reference id list for this search criteria
			this.cache.put(SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_SCHEMES.name()
					+ CollectionsUtils.join(StringPool.COMMA, systemActionName, schemeIdList),
					systemActionMappingIdentifierListBuilder.build(), SYSTEM_ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void removeSystemActionByIdentifier(final String identifier) {

		try {

			final List<SystemActionMappingReferenceCache> referenceCaches =
					(List<SystemActionMappingReferenceCache>)this.cache.get(SYSTEM_ACTION_KEY_REFERENCES+identifier, SYSTEM_ACTION_GROUP);

			if (UtilMethods.isSet(referenceCaches)) {
				// remove all keys where the system action mapping is being referrer.
				removeSystemActionReferences (referenceCaches);
			}

			cache.remove(SYSTEM_ACTION_KEY_REFERENCES + identifier, SYSTEM_ACTION_GROUP);
			cache.remove(SYSTEM_ACTION_KEY_MAIN       + identifier, SYSTEM_ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void removeSystemActionsByWorkflowAction(final String workflowActionId) {

		try {

			// removing by just workflow id
			final String workflowReferenceId      = SystemActionMappingReferenceCache.TYPE.WORKFLOW.name()+workflowActionId;
			final List<String> systemActionIdList = (List<String>)this.cache.get(workflowReferenceId, SYSTEM_ACTION_GROUP);
			if (UtilMethods.isSet(systemActionIdList)) {

				for (final String systemActionMappingIdentifier : systemActionIdList) {

					cache.remove(SYSTEM_ACTION_KEY_MAIN + systemActionMappingIdentifier, SYSTEM_ACTION_GROUP);
				}
			}

			this.cache.remove(workflowReferenceId, SYSTEM_ACTION_GROUP);
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void removeSystemActionsByScheme(final String schemeId) {

		try {

			// removing just by scheme
			final String schemeReferenceId      = SystemActionMappingReferenceCache.TYPE.SCHEME.name()+schemeId;
			List<String> systemActionIdList = (List<String>)this.cache.get(schemeReferenceId, SYSTEM_ACTION_GROUP);
			if (UtilMethods.isSet(systemActionIdList)) {

				for (final String systemActionMappingIdentifier : systemActionIdList) {

					cache.remove(SYSTEM_ACTION_KEY_MAIN + systemActionMappingIdentifier, SYSTEM_ACTION_GROUP);
				}
			}

			this.cache.remove(schemeReferenceId, SYSTEM_ACTION_GROUP);

			// removing system action + scheme
			for (final WorkflowAPI.SystemAction systemAction : WorkflowAPI.SystemAction.values()) {

				final String actionSchemeReferenceId = SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_SCHEMES.name() +
						StringUtils.join(new String[] {systemAction.name(), schemeId}, StringPool.COMMA);
				systemActionIdList = (List<String>) this.cache.get(actionSchemeReferenceId, SYSTEM_ACTION_GROUP);
				if (UtilMethods.isSet(systemActionIdList)) {

					for (final String systemActionMappingIdentifier : systemActionIdList) {

						cache.remove(SYSTEM_ACTION_KEY_MAIN + systemActionMappingIdentifier, SYSTEM_ACTION_GROUP);
					}
				}

				this.cache.remove(actionSchemeReferenceId, SYSTEM_ACTION_GROUP);
			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	@Override
	public void removeSystemActionsByContentType(final String variable) {

		try {

			// removing just by content type
			final String schemeReferenceId      = SystemActionMappingReferenceCache.TYPE.CONTENTTYPE.name()+variable;
			List<String> systemActionIdList = (List<String>)this.cache.get(schemeReferenceId, SYSTEM_ACTION_GROUP);
			if (UtilMethods.isSet(systemActionIdList)) {

				for (final String systemActionMappingIdentifier : systemActionIdList) {

					cache.remove(SYSTEM_ACTION_KEY_MAIN + systemActionMappingIdentifier, SYSTEM_ACTION_GROUP);
				}
			}

			this.cache.remove(schemeReferenceId, SYSTEM_ACTION_GROUP);

			// removing system action + scheme
			for (final WorkflowAPI.SystemAction systemAction : WorkflowAPI.SystemAction.values()) {

				final String actionSchemeReferenceId = SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_CONTENTTYPE.name() +
						StringUtils.join(new String[] {systemAction.name(), variable}, StringPool.COMMA);
				systemActionIdList = (List<String>) this.cache.get(actionSchemeReferenceId, SYSTEM_ACTION_GROUP);
				if (UtilMethods.isSet(systemActionIdList)) {

					for (final String systemActionMappingIdentifier : systemActionIdList) {

						cache.remove(SYSTEM_ACTION_KEY_MAIN + systemActionMappingIdentifier, SYSTEM_ACTION_GROUP);
					}
				}

				this.cache.remove(actionSchemeReferenceId, SYSTEM_ACTION_GROUP);
			}
		} catch (DotCacheException e) {
			Logger.debug(WorkflowCacheImpl.class,e.getMessage(), e);
		}
	}

	private void removeSystemActionReferences(final List<SystemActionMappingReferenceCache> referenceCaches) {

		for (final SystemActionMappingReferenceCache referenceCache : referenceCaches) {

			final SystemActionMappingReferenceCache.TYPE type = referenceCache.getType();
			switch (type) {

				case SCHEME:
					this.cache.remove(SystemActionMappingReferenceCache.TYPE.SCHEME.name()+referenceCache.getKey(),      SYSTEM_ACTION_GROUP);
					break;
				case CONTENTTYPE:
					this.cache.remove(SystemActionMappingReferenceCache.TYPE.CONTENTTYPE.name()+referenceCache.getKey(), SYSTEM_ACTION_GROUP);
					break;
				case WORKFLOW:
					this.cache.remove(SystemActionMappingReferenceCache.TYPE.WORKFLOW.name()+referenceCache.getKey(),    SYSTEM_ACTION_GROUP);
					break;
				case SYSTEMACTION_CONTENTTYPE:
					this.cache.remove(SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_CONTENTTYPE.name()
							+ StringUtils.join(referenceCache.getKeys(), StringPool.COMMA), SYSTEM_ACTION_GROUP);
					break;
				case SYSTEMACTION_SCHEMES:
					this.cache.remove(SystemActionMappingReferenceCache.TYPE.SYSTEMACTION_SCHEMES.name()
							+ StringUtils.join(referenceCache.getKeys(), StringPool.COMMA), SYSTEM_ACTION_GROUP);
					break;
			}
		}
	}
}