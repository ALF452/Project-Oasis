package com.oasis.tracker.update

import com.oasis.tracker.BuildConfig

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotesUrl: String?
)

/**
 * Compares the latest GitHub release against the running build. Releases are
 * tagged "vNN" where NN is the versionCode (the CI run number), so a simple
 * numeric comparison tells us whether a newer APK is available.
 */
class UpdateChecker(private val api: GitHubApi = GitHubApi.create()) {

    suspend fun checkForUpdate(): UpdateInfo? {
        val release = runCatching {
            api.latestRelease(BuildConfig.GITHUB_OWNER, BuildConfig.GITHUB_REPO)
        }.getOrNull() ?: return null

        val remoteVersionCode = parseVersionCode(release.tag_name) ?: return null
        if (remoteVersionCode <= BuildConfig.VERSION_CODE) return null

        val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") } ?: return null

        return UpdateInfo(
            versionCode = remoteVersionCode,
            versionName = release.tag_name,
            downloadUrl = apkAsset.browser_download_url,
            releaseNotesUrl = release.html_url
        )
    }

    private fun parseVersionCode(tag: String): Int? =
        Regex("(\\d+)").find(tag)?.value?.toIntOrNull()
}
