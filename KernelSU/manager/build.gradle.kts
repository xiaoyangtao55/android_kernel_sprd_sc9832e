plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

extra["androidMinSdkVersion"] = 29
extra["androidTargetSdkVersion"] = 37
extra["androidCompileSdkVersion"] = 37
extra["androidCompileSdkVersionMinor"] = 0
extra["androidBuildToolsVersion"] = "37.0.0"
extra["androidCompileNdkVersion"] = libs.versions.ndk.get()
extra["androidSourceCompatibility"] = JavaVersion.VERSION_21
extra["androidTargetCompatibility"] = JavaVersion.VERSION_21
extra["managerVersionCode"] = getVersionCode()
extra["managerVersionName"] = getVersionName()

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec(arrayOf("git", "rev-list", "--count", "HEAD"))
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

fun getGitDescribe(): String {
    val process = Runtime.getRuntime().exec(arrayOf("git", "describe", "--tags", "--always"))
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

// KernelSU is vendored into this kernel tree, so `git rev-list --count HEAD` counts the
// *kernel* repository's squashed history instead of KernelSU's. That silently yields a version
// code which no longer matches the KSU_VERSION the kernel module is compiled with (see
// KernelSU/kernel/Makefile), and Android then refuses to install the manager over an existing
// one. The build environment pins both values from KernelSU/SOURCE_REVISION to keep them in sync.
fun pinnedVersionCode(): Int? = System.getenv("KSU_VERSION_CODE")?.trim()?.toIntOrNull()

fun pinnedVersionName(): String? =
    System.getenv("KSU_VERSION_NAME")?.trim()?.takeIf { it.isNotEmpty() }

fun getVersionCode(): Int {
    pinnedVersionCode()?.let { return it }
    val commitCount = getGitCommitCount()
    return 30000 + commitCount - 95
}

fun getVersionName(): String {
    pinnedVersionName()?.let { return it }
    return getGitDescribe()
}
