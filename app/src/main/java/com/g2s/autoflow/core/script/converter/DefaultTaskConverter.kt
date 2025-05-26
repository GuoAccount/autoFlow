package com.g2s.autoflow.core.script.converter

import com.g2s.autoflow.core.script.model.TaskScript
import com.g2s.autoflow.core.task.Task
import com.g2s.autoflow.core.task.impl.*
import com.g2s.autoflow.utils.AppLogger

/**
 * 默认任务转换器实现
 */
class DefaultTaskConverter : TaskConverter {
    private val tag = "DefaultTaskConverter"

    override fun convert(step: TaskScript.Step, variables: Map<String, Any>): List<Task> {
        return try {
            when (step) {
                is TaskScript.Step.Click -> convertClick(step, variables)
                is TaskScript.Step.Input -> convertInput(step, variables)
                is TaskScript.Step.Swipe -> convertSwipe(step, variables)
                is TaskScript.Step.Wait -> convertWait(step, variables)
                is TaskScript.Step.Condition -> convertCondition(step, variables)
                is TaskScript.Step.Loop -> convertLoop(step, variables)
                is TaskScript.Step.Shell -> convertShell(step, variables)
                else -> emptyList()
            }
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to convert step: ${step::class.simpleName}", e)
            emptyList()
        }
    }

    private fun convertClick(step: TaskScript.Step.Click, variables: Map<String, Any>): List<Task> {
        val task = ClickTask(
            id = generateTaskId("click"),
            targetId = step.targetId?.let { replaceVariables(it, variables) },
            text = step.text?.let { replaceVariables(it, variables) },
            contentDescription = step.contentDescription?.let { replaceVariables(it, variables) },
            useOcr = step.useOcr,
            waitAfter = step.waitAfter,
            timeout = step.timeout,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun convertInput(step: TaskScript.Step.Input, variables: Map<String, Any>): List<Task> {
        val task = InputTask(
            id = generateTaskId("input"),
            targetId = step.targetId?.let { replaceVariables(it, variables) },
            text = step.text?.let { replaceVariables(it, variables) },
            input = replaceVariables(step.input, variables),
            append = step.append,
            waitAfter = step.waitAfter,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun convertSwipe(step: TaskScript.Step.Swipe, variables: Map<String, Any>): List<Task> {
        val task = SwipeTask(
            id = generateTaskId("swipe"),
            startX = step.startX,
            startY = step.startY,
            endX = step.endX,
            endY = step.endY,
            duration = step.duration,
            waitAfter = step.waitAfter,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun convertWait(step: TaskScript.Step.Wait, variables: Map<String, Any>): List<Task> {
        val task = WaitTask(
            id = generateTaskId("wait"),
            milliseconds = step.milliseconds,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun convertCondition(
        step: TaskScript.Step.Condition,
        variables: Map<String, Any>
    ): List<Task> {
        val condition = replaceVariables(step.condition, variables)
        val thenTasks = step.then.flatMap { convert(it, variables) }
        val elseTasks = step.`else`.flatMap { convert(it, variables) }
        
        val task = ConditionTask(
            id = generateTaskId("condition"),
            condition = condition,
            thenTasks = thenTasks,
            elseTasks = elseTasks,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun convertLoop(step: TaskScript.Step.Loop, variables: Map<String, Any>): List<Task> {
        val loopCondition = step.`while`?.let { replaceVariables(it, variables) }
        val loopTasks = step.steps.flatMap { convert(it, variables) }
        
        val task = LoopTask(
            id = generateTaskId("loop"),
            times = step.times,
            condition = loopCondition,
            tasks = loopTasks,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun convertShell(step: TaskScript.Step.Shell, variables: Map<String, Any>): List<Task> {
        val task = ShellTask(
            id = generateTaskId("shell"),
            command = replaceVariables(step.command, variables),
            useRoot = step.useRoot,
            timeout = step.timeout,
            description = replaceVariables(step.description, variables)
        )
        return listOf(task)
    }

    private fun generateTaskId(prefix: String): String {
        return "${prefix}_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}
