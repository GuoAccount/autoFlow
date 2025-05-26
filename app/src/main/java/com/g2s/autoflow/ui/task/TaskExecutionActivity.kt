package com.g2s.autoflow.ui.task

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.g2s.autoflow.R
import com.g2s.autoflow.core.script.ScriptManager
import com.g2s.autoflow.core.task.DefaultTaskExecutor
import com.g2s.autoflow.utils.AppLogger
import android.widget.Toast
import com.g2s.autoflow.databinding.ActivityTaskExecutionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TaskExecutionActivity : AppCompatActivity() {
    
    private val tag = "TaskExecutionActivity"
    private val taskExecutor = DefaultTaskExecutor()
    private val scriptManager = ScriptManager(taskExecutor)
    
    private lateinit var binding: ActivityTaskExecutionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskExecutionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnExecuteTask.setOnClickListener {
            executeDouyinTask()
        }
    }
    
    private fun executeDouyinTask() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 从 assets 加载任务脚本
                val inputStream = assets.open("tasks/open_douyin.yaml")
                val tempFile = File(cacheDir, "temp_task.yaml")
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                
                // 执行任务
                scriptManager.loadAndExecuteScript(
                    file = tempFile,
                    onSuccess = { 
                        AppLogger.d(tag, "任务执行成功")
                        runOnUiThread {
                            Toast.makeText(this@TaskExecutionActivity, "任务执行成功", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error ->
                        AppLogger.e(tag, "任务执行失败", error)
                        runOnUiThread {
                            Toast.makeText(this@TaskExecutionActivity, "任务执行失败: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            } catch (e: Exception) {
                AppLogger.e(tag, "执行任务时出错", e)
            }
        }
    }
}
