/**
 * 
 */
package cn.bc.workflow.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import cn.bc.workflow.domain.ExcutionLog;

/**
 * 记录流程实例的启动、结束日志的监听器
 * 
 * @author dragon
 * 
 */
public class ProcessLogListener extends ExcutionLogListener {
	@Override
	protected String getLogTypePrefix() {
		return "process_";
	}

	@Override
	protected ExcutionLog buildExcutionLog(DelegateExecution execution) {
		ExcutionLog log = super.buildExcutionLog(execution);

		// 记录流程的编码
		if (execution instanceof ExecutionEntity) {
			ExecutionEntity e = (ExecutionEntity) execution;
			log.setCode(e.getProcessDefinitionId());
		}

		return log;
	}
}