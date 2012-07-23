package cn.bc.workflow.dao.hibernate.jpa;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cn.bc.core.exception.CoreException;
import cn.bc.core.query.condition.impl.AndCondition;
import cn.bc.core.query.condition.impl.EqualsCondition;
import cn.bc.core.util.DateUtils;
import cn.bc.core.util.JsonUtils;
import cn.bc.orm.hibernate.jpa.HibernateCrudJpaDao;
import cn.bc.orm.hibernate.jpa.HibernateJpaNativeQuery;
import cn.bc.workflow.dao.ExcutionLogDao;
import cn.bc.workflow.domain.ExcutionLog;

/**
 * 流转日志Dao接口的实现
 * 
 * @author dragon
 * 
 */
public class ExcutionLogDaoImpl extends HibernateCrudJpaDao<ExcutionLog>
		implements ExcutionLogDao, InitializingBean, ApplicationContextAware {
	private static Log logger = LogFactory.getLog(ExcutionLogDaoImpl.class);
	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public HistoryService getHistoryService() {
		return applicationContext.getBean(HistoryService.class);
	}

	public TaskService getTaskService() {
		return applicationContext.getBean(TaskService.class);
	}

	public void afterPropertiesSet() throws Exception {
	}

	public ExcutionLog loadByTask(String taskId, String type) {
		return this
				.createQuery()
				.condition(
						new AndCondition().add(
								new EqualsCondition("taskInstanceId", taskId))
								.add(new EqualsCondition("type", type)))
				.singleResult();
	}

	public String findTaskFormKey(String taskId) {
		if (taskId == null || taskId.length() == 0)
			return null;
		String hql = "select id,form_ from bc_wf_excution_log where tid=?";
		if (logger.isDebugEnabled()) {
			logger.debug("hql=" + hql);
			logger.debug("taskId=" + taskId);
		}
		List<Object[]> all = HibernateJpaNativeQuery.executeNativeSql(
				getJpaTemplate(), hql, new Object[] { taskId }, null);
		if (all == null || all.isEmpty()) {
			return null;
		} else {
			return (String) all.get(0)[1];
		}
	}

	public Map<String, String> findTaskFormKeys(String processInstanceId) {
		if (processInstanceId == null || processInstanceId.length() == 0)
			return new LinkedHashMap<String, String>();
		String hql = "select tid, form_ from bc_wf_excution_log where pid=? and tid is not null and form_ is not null order by file_date";
		if (logger.isDebugEnabled()) {
			logger.debug("hql=" + hql);
			logger.debug("args=" + processInstanceId);
		}
		List<Object[]> all = HibernateJpaNativeQuery
				.executeNativeSql(getJpaTemplate(), hql,
						new Object[] { processInstanceId }, null);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (Object[] o : all) {
			map.put(o[0].toString(), o[1].toString());
		}
		return map;
	}

	public Map<String, Object> findTaskVariables(String taskId) {
		// activiti的流程变量存储规则：
		// 流传中的流程，所有任务的流程变量是放在act_ru_variable表中的，
		// 在act_hi_detail表中并不存在，知道流程结束后才会移动到act_hi_detail表中

		// 获取任务信息：用于判断流程的状态
		HistoricTaskInstance task = getHistoryService()
				.createHistoricTaskInstanceQuery().taskId(taskId)
				.singleResult();
		if (task == null) {
			throw new CoreException("can't find taskHistory: id=" + taskId);
		}
		HistoricProcessInstance pi = getHistoryService()
				.createHistoricProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId()).singleResult();
		if (pi == null) {
			throw new CoreException("can't find processInstanceHistory: id="
					+ task.getProcessInstanceId());
		}

		Map<String, Object> params = new LinkedHashMap<String, Object>();

		// 全局流程变量
		List<HistoricDetail> detail = getHistoryService()
				.createHistoricDetailQuery().processInstanceId(pi.getId())
				.variableUpdates().list();
		HistoricVariableUpdate v;
		for (HistoricDetail d : detail) {
			v = (HistoricVariableUpdate) d;
			params.put(v.getVariableName(), v.getValue());
		}

		// 本地流程变量
		detail = getHistoryService().createHistoricDetailQuery().taskId(taskId)
				.variableUpdates().list();
		for (HistoricDetail d : detail) {
			v = (HistoricVariableUpdate) d;
			params.put(v.getVariableName(), v.getValue());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("params0=" + params);
		}

		// 转换特殊类型的变量的值
		for (Entry<String, Object> e : params.entrySet()) {
			if (e.getKey().startsWith("list_")
					&& e.getValue() instanceof String) {// 将字符串转化为List
				e.setValue(JsonUtils.toCollection((String) e.getValue()));
			} else if (e.getKey().startsWith("map_")
					&& e.getValue() instanceof String) {// 将字符串转化为Map
				e.setValue(JsonUtils.toMap((String) e.getValue()));
			} else if (e.getKey().startsWith("array_")
					&& e.getValue() instanceof String) {// 将字符串转化为数组
				e.setValue(JsonUtils.toArray((String) e.getValue()));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("params1=" + params);
		}

		return params;
	}
}