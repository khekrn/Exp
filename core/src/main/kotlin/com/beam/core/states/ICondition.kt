package com.beam.core.states

/**
 * @author KK
 */

interface ICondition {
    val variable: String
    val conditionType: ConditionType
    val matchValue: String
    val nexState: String
}