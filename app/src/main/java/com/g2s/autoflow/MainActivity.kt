package com.g2s.autoflow

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.g2s.autoflow.databinding.ActivityMainBinding

/**
 * 主活动类，负责应用的主界面展示和导航控制
 * 使用Navigation组件管理Fragment之间的导航
 */

class MainActivity : AppCompatActivity() {

    // 应用栏配置，用于管理顶部应用栏的行为
    private lateinit var appBarConfiguration: AppBarConfiguration
    // 视图绑定实例，用于访问布局中的视图
    private lateinit var binding: ActivityMainBinding

    /**
     * 活动创建时调用，初始化UI和导航
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化视图绑定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.appBarMain.toolbar)


        // 设置浮动操作按钮点击事件
        binding.appBarMain.fab.setOnClickListener { _ ->
            // 启动任务执行Activity
            val intent = android.content.Intent(this, com.g2s.autoflow.ui.task.TaskExecutionActivity::class.java)
            startActivity(intent)
        }
        // 获取抽屉布局和导航视图
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        
        // 获取NavController，用于管理Fragment导航
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        
        // 配置应用栏，指定哪些目标应该是顶级目标（不显示返回按钮）
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,     // 主页
                R.id.nav_gallery,  // 图库
                R.id.nav_slideshow // 幻灯片
            ), drawerLayout
        )
        
        // 设置ActionBar与NavController的关联
        setupActionBarWithNavController(navController, appBarConfiguration)
        // 设置导航视图与NavController的关联
        navView.setupWithNavController(navController)
    }

    /**
     * 创建选项菜单
     * @param menu 菜单对象
     * @return 返回true显示菜单
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // 加载菜单布局
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * 处理导航返回按钮点击事件
     * @return 如果事件被处理返回true，否则返回false
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // 尝试处理返回导航，如果无法处理则调用父类方法
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}