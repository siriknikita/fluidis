package com.fluidis.app.feature.history.components

import androidx.compose.ui.graphics.Color
import com.fluidis.app.core.model.GoalStatus
import com.fluidis.app.core.theme.DotBelowGoal
import com.fluidis.app.core.theme.DotGoalMet
import com.fluidis.app.core.theme.DotOverUpper

val GoalStatus.color: Color
    get() = when (this) {
        GoalStatus.BELOW -> DotBelowGoal
        GoalStatus.MET -> DotGoalMet
        GoalStatus.OVER -> DotOverUpper
    }
