package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'mutationmaven_3_5_jdk_openjdk_10'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("mutationmaven_3_5_jdk_openjdk_10")) {
    check(artifactRules == "") {
        "Unexpected option value: artifactRules = $artifactRules"
    }
    artifactRules = "target/pit-reports => target/pit-reports"
}