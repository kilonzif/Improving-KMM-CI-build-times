object SetEnvParams {
    //Configure Separate host environment
    val splitTargets: Boolean get() = System.getProperty("split_targets") != null
}

fun KotlinTarget.getHostType(): HostTarget? =
    when (platformType) {
        KotlinPlatformType.androidJvm,
        KotlinPlatformType.jvm,
        KotlinPlatformType.js -> HostType.LINUX

        KotlinPlatformType.native ->
            when {
                name.startsWith("ios") -> HostType.MAC_OS
                name.startsWith("watchos") -> HostType.MAC_OS
                name.startsWith("linux") -> HostType.LINUX
                else -> error("Unsupported native target: $this")
            }

        KotlinPlatformType.common -> null
    }

enum class HostTarget {
    MAC_OS, LINUX
}

fun KotlinTarget.isCompilationAllowed(): Boolean {
    if ((name == KotlinMultiplatformPlugin.METADATA_TARGET_NAME) || !SetEnvParams.splitTargets) {
        return true
    }

    val os = OperatingSystem.current()

    return when (getHostType()) {
        HostType.MAC_OS -> os.isMacOsX
        HostType.LINUX -> os.isLinux
        null -> true
    }
}

fun KotlinTarget.disableCompilationsIfNeeded() {
    if (!isCompilationAllowed()) {
        disableCompilations()
    }
}

private fun KotlinTarget.disableCompilations() {
    compilations.configureEach {
        compileKotlinTask.enabled = false
    }
}
