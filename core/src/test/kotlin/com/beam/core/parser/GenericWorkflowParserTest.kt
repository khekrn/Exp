package com.beam.core.parser

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

/**
 * @author KK
 */
internal class GenericWorkflowParserTest {

    var filePath: String = ""

    private val genericWorkflowParser = GenericWorkflowParser()

    @BeforeEach
    fun setUp() {
        filePath = this.javaClass.classLoader.getResource("sample-workflow.json")!!.path
    }

    @Test
    fun parseWorkflow() {
        val workflowGraph = genericWorkflowParser.parseWorkflow(filePath)
        assertNotNull(workflowGraph)
    }
}