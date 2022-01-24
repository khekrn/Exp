package com.beam.core.domain

import com.beam.core.states.IState
import com.beam.core.states.StateType

/**
 * @author KK
 */

data class WaitState(
    override val name: String,
    override val type: StateType = StateType.Wait,
    override val previousState: String?,
    override val nextState: String?,
    override val isEnd: Boolean = false
) : IState