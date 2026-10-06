# F-Droid publishing guide

AmniGuard Firewall can be built from source for F-Droid with the `fdroid`
product flavor. The flavor removes the Google Play Billing permission and
keeps `PLAY_STORE_RELEASE` disabled; the app's firewall and VPN code remain
local to the device.

## Release prerequisites

1. Keep the root `LICENSE` file and the Fastlane metadata under
   `fastlane/metadata/android/en-US/` in each release.
2. Create an annotated release tag after updating `versionName` and
   `versionCode` in `app/build.gradle`, for example:

   ```bash
   git tag -a v0.0.1 -m "Release v0.0.1"
   git push origin v0.0.1
   ```

3. Verify the F-Droid variant locally:

   ```bash
   ./gradlew :app:assembleFdroidRelease :app:lintFdroidRelease --no-daemon
   ```

## fdroiddata metadata

Save the following as `metadata/com.alhaq.amniguard.yml` in a fork of the
`fdroiddata` repository. Update the version fields and tag for every release.

```yaml
Categories:
  - Security
License: GPL-3.0-only
AuthorName: Alhaq Initiative
SourceCode: https://github.com/alhaq-studio/amniguard-firewall
IssueTracker: https://github.com/alhaq-studio/amniguard-firewall/issues
Changelog: https://github.com/alhaq-studio/amniguard-firewall/blob/main/CHANGELOG.md
Donate: https://alhaq-initiative.org/donate

RepoType: git
Repo: https://github.com/alhaq-studio/amniguard-firewall.git

Builds:
  - versionName: '0.0.1'
    versionCode: 1
    commit: v0.0.1
    subdir: app
    gradle:
      - yes
    gradleTasks:
      - assembleFdroidRelease

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: '0.0.1'
CurrentVersionCode: 1
```

F-Droid maintains the metadata in `fdroiddata`, not in this application
repository. Submit the metadata as a merge request to
<https://gitlab.com/fdroid/fdroiddata> after the tagged source release is
available.
