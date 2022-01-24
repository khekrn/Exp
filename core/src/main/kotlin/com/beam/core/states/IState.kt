package com.beam.core.states

/**
 * @author KK
 */

interface IState {
    val name: String
    val type: StateType
    val previousState: String?
    val nextState: String?
    val isEnd: Boolean
}