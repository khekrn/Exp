package com.beam.groot

import com.beam.engine.service.WorkflowService
import com.beam.engine.task.ITask
import kotlinx.coroutines.delay
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(scanBasePackages = ["com.beam.engine", "com.beam.groot"])
@EnableR2dbcRepositories(basePackages = ["com.beam.engine"])
class GrootApplication

fun main(args: Array<String>) {
    runApplication<GrootApplication>(*args)
}

@Component("T1")
class TaskT1 : ITask {
    override suspend fun executeTask() {
        println("Starting to run task")
        delay(5000)
        println("Execution is complete")
    }

}

@RestController
class FunController(val service: WorkflowService) {
    @GetMapping("/greet")
    suspend fun greet(): String{
        service.executeWorkflow()
        return "Hello Spring"
    }
}
