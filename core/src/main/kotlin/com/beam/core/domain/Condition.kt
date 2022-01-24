package com.beam.core.domain

import com.beam.core.states.ConditionType
import com.beam.core.states.ICondition

/**
 * @author KK
 */

data class Condition(
    override val variable: String,
    override val conditionType: ConditionType,
    override val matchValue: String,
    override val nexState: String,
) : ICondition
