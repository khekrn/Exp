package com.beam.core.domain

import com.beam.core.states.IState

/**
 * @author KK
 */

class WorkflowGraph {
    var workflowName: String = ""
    var isAsync: Boolean = false
    var startState: String = ""
    var resultVariable: String = ""
    var statesMap: Map<String, IState> = emptyMap()
}