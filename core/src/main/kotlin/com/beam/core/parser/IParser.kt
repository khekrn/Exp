package com.beam.core.parser

import com.beam.core.domain.WorkflowGraph

/**
 * @author KK
 */

interface IParser {

    fun parseWorkflow(file: String): WorkflowGraph
}