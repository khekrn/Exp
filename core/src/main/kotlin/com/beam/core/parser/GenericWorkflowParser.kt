package com.beam.core.parser

import com.beam.core.domain.*
import com.beam.core.domain.Constants.ERR_INVALID_JSON_FILE
import com.beam.core.domain.Constants.ERR_INVALID_WORKFLOW
import com.beam.core.domain.Constants.ERR_REQUIRED_INFO_MISSING
import com.beam.core.domain.Constants.LABEL_ASYNC
import com.beam.core.domain.Constants.LABEL_CONDITIONS
import com.beam.core.domain.Constants.LABEL_CONDITION_MATCH_TYPE
import com.beam.core.domain.Constants.LABEL_CONDITION_MATCH_VALUE
import com.beam.core.domain.Constants.LABEL_CONDITION_VARIABLE
import com.beam.core.domain.Constants.LABEL_DESCRIPTION
import com.beam.core.domain.Constants.LABEL_END
import com.beam.core.domain.Constants.LABEL_INIT_STATE
import com.beam.core.domain.Constants.LABEL_NAME
import com.beam.core.domain.Constants.LABEL_NEXT
import com.beam.core.domain.Constants.LABEL_RESULT_VARIABLE
import com.beam.core.domain.Constants.LABEL_STATES
import com.beam.core.domain.Constants.LABEL_TYPE
import com.beam.core.exception.JsonException
import com.beam.core.exception.WorkflowParserError
import com.beam.core.states.ConditionType
import com.beam.core.states.IState
import com.beam.core.states.StateType
import com.beam.core.utils.JsonUtils
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author KK
 *
 * Create the workflow state machine for the given JSON file
 */

class GenericWorkflowParser : IParser {

    private val logger = LoggerFactory.getLogger(GenericWorkflowParser::class.java)

    /**
     * Creates the [WorkflowGraph] state machine instance from the given JSON file
     *
     * @param file name of the workflow json file
     */
    override fun parseWorkflow(file: String): WorkflowGraph {
        logger.info("In parseWorkflow")
        logger.info("Parsing file - {}", file)

        val workflowGraph = WorkflowGraph()
        val workflowJson = File(file).readText()
        val workflowStatesDictionary: MutableMap<String, IState> = LinkedHashMap()
        val workflowName: String
        val startState: String
        val isAsync: Boolean
        try {
            val jsonNode = JsonUtils.fromJson(workflowJson, JsonNode::class.java)
            logger.info("Checking for all required fields in workflow JSON")
            validateFields(jsonNode)

            // Setting the workflow name
            workflowName = jsonNode.get(LABEL_NAME).asText()
            workflowGraph.workflowName = workflowName

            // Identifying start state
            startState = jsonNode.get(LABEL_INIT_STATE).asText()
            workflowGraph.startState = startState

            // Setting Async or Sync
            isAsync = jsonNode.get(LABEL_ASYNC).asBoolean()
            workflowGraph.isAsync = isAsync

            // Setting the result variable
            if (!isAsync) {
                workflowGraph.resultVariable = jsonNode.get(LABEL_RESULT_VARIABLE).asText()
            }

            val iterator: Iterator<Map.Entry<String, JsonNode>> = jsonNode.get(LABEL_STATES).fields()
            val componentNameSet = mutableSetOf<String>()
            var previousState: String? = null
            while (iterator.hasNext()) {
                val stateEntry = iterator.next()
                val componentName = stateEntry.key
                val stateNode = stateEntry.value

                if (!stateNode.has(LABEL_TYPE)) {
                    logger.error("Error: Every state should have $LABEL_TYPE field")
                    throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_TYPE")
                }

                if (!stateNode.has(LABEL_END)) {
                    logger.error("Error: Every state should have $LABEL_END field")
                    throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_END")
                }

                val isEnd = stateNode.get(LABEL_END).asBoolean()
                val stateType = stateNode.get(LABEL_TYPE).asText()
                val stateInstance: IState
                when (stateType) {
                    StateType.Task.name -> {
                        stateInstance =
                            TaskState(
                                name = componentName,
                                type = StateType.Task,
                                previousState = previousState,
                                nextState = stateNode.get(LABEL_NEXT).asText(),
                                isEnd = isEnd
                            )
                        validateState(stateInstance.nextState!!, componentNameSet)
                    }
                    StateType.Condition.name -> {
                        if (isEnd) {
                            logger.error("Error: We cannot end the workflow with ${StateType.Condition.name} state")
                            throw WorkflowParserError("$ERR_INVALID_WORKFLOW, Workflow cannot end with ${StateType.Condition.name} state")
                        }

                        if (!stateNode.has(LABEL_CONDITIONS)) {
                            logger.error("Error: Required $LABEL_CONDITIONS label does not exist in  ${StateType.Condition.name} state")
                            throw WorkflowParserError("$ERR_INVALID_WORKFLOW, $LABEL_CONDITIONS label does not exist")
                        }

                        stateInstance = ConditionsState(
                            previousState = previousState,
                            conditionList = extractConditions(stateNode.get(LABEL_CONDITIONS), componentNameSet)
                        )
                    }
                    StateType.Wait.name -> {
                        if (isEnd) {
                            logger.error("Error: We cannot end the workflow with ${StateType.Wait.name} state")
                            throw WorkflowParserError("$ERR_INVALID_WORKFLOW, Workflow cannot end with ${StateType.Wait.name} state")
                        }
                        stateInstance =
                            WaitState(
                                name = componentName,
                                previousState = previousState,
                                nextState = stateNode.get(LABEL_NEXT).asText()
                            )
                        validateState(stateInstance.nextState!!, componentNameSet)
                    }
                    else -> {
                        logger.error("Error: Invalid or Unknown task name found ${stateNode.get(LABEL_TYPE).asText()}")
                        throw WorkflowParserError(
                            "$ERR_INVALID_WORKFLOW, Invalid task name found ${
                                stateNode.get(
                                    LABEL_TYPE
                                ).asText()
                            }"
                        )
                    }
                }
                previousState = componentName

                componentNameSet.add(componentName)
                workflowStatesDictionary[componentName] = stateInstance
            }

            // Setting all workflow states
            workflowGraph.statesMap = workflowStatesDictionary
        } catch (ex: JsonException) {
            logger.error(
                "Error: Problem while parsing the workflow, cannot able to create workflow graph -{}",
                ex.message
            )
            throw WorkflowParserError("$ERR_INVALID_JSON_FILE $file", ex)
        }

        return workflowGraph
    }

    private fun validateState(nextState: String, componentSet: Set<String>) {
        if (StringUtils.isBlank(nextState) && componentSet.contains(nextState)) {
            logger.error("Error: Cannot connect to $nextState, Workflow is wrong !!")
            throw WorkflowParserError("$ERR_INVALID_WORKFLOW, We can't connect to $nextState")
        }
    }

    private fun extractConditions(jsonNode: JsonNode, componentSet: Set<String>): Set<Condition> {
        logger.info("In extractConditions")
        val result = mutableSetOf<Condition>()
        val iterator = jsonNode.iterator()
        while (iterator.hasNext()) {
            val node = iterator.next()
            val variable = node.get(LABEL_CONDITION_VARIABLE).asText()
            val matchType = node.get(LABEL_CONDITION_MATCH_TYPE).asText()
            val conditionType: ConditionType = when (matchType!!) {
                "StringEquals" -> ConditionType.STRING_EQUALS
                "StringNotEquals" -> ConditionType.STRING_NOT_EQUALS
                "BooleanEquals" -> ConditionType.BOOLEAN_EQUALS
                "BooleanNotEquals" -> ConditionType.BOOLEAN_NOT_EQUALS
                else -> {
                    logger.error("Unknown condition type found in the workflow Json - {}", matchType)
                    throw WorkflowParserError("$ERR_INVALID_WORKFLOW, Invalid match type in conditions $matchType")
                }
            }
            val matchValue = node.get(LABEL_CONDITION_MATCH_VALUE).asText()
            val nextState = node.get(LABEL_NEXT).asText()
            if (StringUtils.isBlank(nextState) && componentSet.contains(nextState)) {
                logger.error("Error: Cannot connect to $nextState, Workflow is wrong !!")
                throw WorkflowParserError("$ERR_INVALID_WORKFLOW, We can't connect to $nextState")
            }
            result.add(
                Condition(
                    variable = variable,
                    conditionType = conditionType,
                    matchValue = matchValue,
                    nexState = nextState
                )
            )
        }

        logger.info("Return from extractConditions")
        return result
    }

    /**
     * Function to validate required fields
     *
     * @param jsonNode - Top container node
     */
    private fun validateFields(jsonNode: JsonNode) {
        logger.info("In validateFields")
        logger.info("Workflow Json = {}", jsonNode)

        if (!jsonNode.has(LABEL_NAME)) {
            logger.error("Error: Required label $LABEL_NAME not found in json")
            throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_NAME")
        }

        if (!jsonNode.has(LABEL_DESCRIPTION)) {
            logger.error("Error: Required label $LABEL_DESCRIPTION not found in json")
            throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_DESCRIPTION")
        }

        if (!jsonNode.has(LABEL_ASYNC)) {
            logger.error("Error: Required label $LABEL_ASYNC not found in json")
            throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_ASYNC")
        }

        if (!jsonNode.has(LABEL_STATES)) {
            logger.error("Error: Required label $LABEL_STATES not found in json")
            throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_STATES")
        }

        if (!jsonNode.has(LABEL_INIT_STATE)) {
            logger.error("Error: Required label $LABEL_INIT_STATE not found in json")
            throw WorkflowParserError("$ERR_REQUIRED_INFO_MISSING = $LABEL_INIT_STATE")
        }

        logger.info("Return from validateFields")
    }
}