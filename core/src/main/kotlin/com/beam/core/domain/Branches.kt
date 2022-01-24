package com.beam.core.domain

import com.beam.core.states.IState

/**
 * @author KK
 */

data class Branches(val startAt: String, val stateList: List<IState>)
