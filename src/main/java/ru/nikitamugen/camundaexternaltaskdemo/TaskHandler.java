package ru.nikitamugen.camundaexternaltaskdemo;

import org.apache.logging.log4j.util.Strings;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);

    @Autowired
    ExternalTaskService externalTaskService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Scheduled(fixedRate = 2000)
    public void pollTasks(){
        List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(10, "externalWorkerId")
                .topic("send-request-and-wait-response", 60L * 1000L)
                .execute();

        for (LockedExternalTask task : tasks) {
            String first = (String) task.getVariables().get("first");
            String second = (String) task.getVariables().get("second");
            String third = (String) task.getVariables().get("third");
            String fourth = (String) task.getVariables().get("fourth");

            LOGGER.info("poll task: {}", task.getId());

            if(Strings.isBlank(first)){
                LOGGER.info("first");
                runtimeService.setVariable(task.getExecutionId(),"first", "ok");
                externalTaskService.unlock(task.getId());
            }else if(Strings.isBlank(second)){
                LOGGER.info("second");
                runtimeService.setVariable(task.getExecutionId(),"second", "ok");
                externalTaskService.unlock(task.getId());
            }else if(Strings.isBlank(third)){
                LOGGER.info("third");
                runtimeService.setVariable(task.getExecutionId(),"third", "ok");
                externalTaskService.unlock(task.getId());
            }else if(Strings.isNotBlank(fourth) ){
                LOGGER.info("fourth");
                externalTaskService.complete(task.getId(), "externalWorkerId");
            }else{
                LOGGER.info("error");
                externalTaskService.handleFailure(task.getId(), "externalWorkerId",
                        "Error text ...", 0,
                        100L);
                externalTaskService.unlock(task.getId());
            }
        }
    }
}
