/**
 * 
 */
package cn.bc.workflow.historictaskinstance.service;

import java.util.List;


/**
 * 任务监控Service
 * 
 * @author wis
 */
public interface HistoricTaskInstanceService {

	/**
	 * 查找用户任务中的流程名称
	 * 
	 * @param account 用户账号
	 * @param isDone 是否只查找已完成任务的流程
	 * @return
	 */
	List<String> findProcessNames(String account,boolean isDone);
	
	/**
	 * 查找流程名称
	 * 
	 * @return
	 */
	List<String> findProcessNames();
	
	/**
	 * 查找用户任务名称
	 * 
	 * @param account 用户账号
	 * @param isDone 是否只查找已完成任务的流程
	 * @return
	 */
	List<String> findTaskNames(String account,boolean isDone);
	
	/**
	 * 查找任务名称
	 * 
	 * @return
	 */
	List<String> findTaskNames();
	
	/**
	 * 
	 * @param key 流程的key
	 * @param data json格式 { 
							procinstId:流程实例id,
							procinstName:流程实例名称,
							procinstKey:流程KEY,
							procinstTaskName:任务名称,
							procinstTaskId:任务id  }
		@return 返回流程id
	 */
	String doStartFlow(String key,String data) throws Exception;

	/**
	 * 查找经办人
	 * 
	 * @param processInstanceId
	 *            流程实例id
	 * @return 返回流程经办人列表
	 */
	List<String> findTransactors(String processInstanceId);

	/**
	 * 在欲包含的任务Key中查找经办人
	 * 
	 * @param processInstanceId
	 *            流程实例id
	 * @param includeTaskKeys
	 *            欲包含的TaskKey
	 * @return 返回流程经办人列表
	 */
	List<String> findTransactorsByIncludeTaskKeys(String processInstanceId,
			String[] includeTaskKeys);

	/**
	 * 在欲排除的任务Key中查找经办人
	 * 
	 * @param processInstanceId
	 *            流程实例id
	 * @param exclusiveTaskKeys
	 *            欲排除的TaskKey
	 * @return 返回流程经办人列表
	 */
	List<String> findTransactorsByExclusiveTaskKeys(String processInstanceId,
			String[] exclusiveTaskKeys);
}