package com.beam.engine.service

import com.beam.engine.model.Workflow
import com.beam.engine.repository.WorkflowRepository
import com.beam.engine.task.ITask
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component("workflowService")
open class WorkflowService(private val applicationContext:ApplicationContext, private val repository: WorkflowRepository) {

    suspend fun executeWorkflow(){
        val bean = applicationContext.getBean("T1") as ITask
        bean.executeTask()
        val workflow = Workflow(
            partnerLoanId = "IL342234234",
            partnerId = "IL",
            workflowType = "Offer",
            status = "S",
            remarks = "Aaasdasd",
            request = "Test",
            messageId = "dfsdfsdfsd",
            createdAt = LocalDateTime.now()
        )

        repository.save(workflow)
    }
}