import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import wing.AndroidCommonExtension

class AndroidComposeConfig : AndroidConfig() {


    override fun pluginConfigs(): PluginManager.() -> Unit = {
        super.pluginConfigs()(this)
        //https://developer.android.google.cn/develop/ui/compose/compiler?hl=zh-cn
        apply("org.jetbrains.kotlin.plugin.compose")

    }

    override fun androidExtensionConfig(): AndroidCommonExtension.(Project, VersionCatalog) -> Unit {
        return { project, vlibs ->
            project.extensions.getByType<ComposeCompilerGradlePluginExtension>().apply {
                //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html#compose-compiler-options-dsl
                //Default: false
                //If true, enable Strong Skipping mode.
                //强跳过是一种实验模式，通过跳过参数未发生变化的可组合函数的不必要调用来提高应用的运行时性能。例如，具有不稳定参数的可组合函数将变为可跳过，
                // 具有不稳定捕获的 lambda 将被记忆化。
                //https://developer.android.google.cn/develop/ui/compose/performance/stability/fix?hl=zh-cn#configuration-file
                //https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/strong-skipping.md
                enableStrongSkippingMode = true
                //指定目录后，Compose 编译器将使用该目录转储编译器指标报告。它们对于优化应用的运行时性能非常有用：报告显示哪些可组合函数是可跳过的、可重新启动的、只读的等等。
                reportsDestination = project.layout.buildDirectory.dir("compose_compiler")
                //https://developer.android.google.cn/develop/ui/compose/performance/stability/fix?hl=zh-cn#configuration-file
//                stabilityConfigurationFile = project.rootProject.layout.projectDirectory.file("stability_config.conf")
            }
        }
    }

    override fun dependenciesConfig(): DependencyHandlerScope.(VersionCatalog) -> Unit = { vlibs: VersionCatalog ->
        vlibs.findLibrary("androidx-compose-bom").ifPresent { bom ->
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", vlibs.findBundle("compose").get())
            add("debugImplementation", vlibs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("debugImplementation", vlibs.findLibrary("androidx-compose-ui-tooling").get())
        }
        vlibs.findLibrary("androidx-compose-ui-test-manifest").ifPresent { androidxCompose ->
            add("androidTestImplementation", androidxCompose)
            add("debugImplementation", androidxCompose)
        }
    }
}

