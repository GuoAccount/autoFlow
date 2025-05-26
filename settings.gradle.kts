// 插件管理块，用于配置Gradle插件的仓库和解析行为
pluginManagement {
    // 配置插件仓库
    repositories {
        // 阿里云镜像仓库(主用) - 加速Gradle插件下载
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }  // Gradle插件仓库镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }         // 公共仓库镜像
        maven { url = uri("https://maven.aliyun.com/repository/google") }         // Google仓库镜像
        
        // 官方仓库（备用）
        google {
            // 内容过滤，只包含Android相关组
            content {
                includeGroupByRegex("com\\.android.*")  // 包含所有com.android开头的组
                includeGroupByRegex("com\\.google.*")  // 包含所有com.google开头的组
                includeGroupByRegex("androidx.*")       // 包含所有androidx开头的组
            }
        }
        mavenCentral()      // Maven中央仓库
        gradlePluginPortal() // Gradle插件门户
    }
}

// 依赖解析管理，用于配置项目依赖的仓库和解析行为
dependencyResolutionManagement {
    // 设置仓库模式：FAIL_ON_PROJECT_REPOS表示如果项目中的build.gradle.kts也声明了仓库，则构建失败
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    
    // 配置项目依赖仓库
    repositories {
        // 阿里云镜像仓库(主用) - 加速依赖下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }         // 公共仓库镜像
        maven { url = uri("https://maven.aliyun.com/repository/google") }         // Google仓库镜像
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }  // Gradle插件仓库镜像
        
        // 官方仓库（备用）
        google()         // Google官方仓库
        mavenCentral()  // Maven中央仓库
    }
}

// 根项目名称
rootProject.name = "autoFlow"

// 包含的子项目模块
include(":app")  // 包含app模块
