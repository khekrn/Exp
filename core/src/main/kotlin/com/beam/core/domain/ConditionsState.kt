package com.beam.core.domain

import com.beam.core.states.IState
import com.beam.core.states.StateType

/**
 * @author KK
 */

data class ConditionsState(
    override val name: String = "Condition_No_Name",
    override val type: StateType = StateType.Condition,
    override val previousState: String?,
    override val nextState: String? = "",
    override val isEnd: Boolean = false,
    val conditionList: Set<Condition>
) : IState
