package com.beam.core.domain

import com.beam.core.states.IState
import com.beam.core.states.StateType

/**
 * @author KK
 */

data class TaskState(
    override val name: String,
    override val type: StateType = StateType.Task,
    override val previousState: String?,
    override val nextState: String?,
    override val isEnd: Boolean = false
) : IState
