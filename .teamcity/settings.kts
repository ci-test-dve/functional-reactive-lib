import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_1.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.1"

project {

    val jdks = listOf("svenruppert/maven-3.5-jdk-openjdk-10", "svenruppert/maven-3.5-jdk-openjdk-11")

    jdks.forEach { buildType(createBuild(it)) }

}

fun createBuild(jdk: String): BuildType {
    val buildType = BuildType({
        name = "Build with - $jdk"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            maven {
                goals = "clean test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                mavenVersion = defaultProvidedVersion()
                dockerImage = jdk
                param("teamcity.tool.jacoco", "%teamcity.tool.jacoco.DEFAULT%")
            }
        }

        triggers {
            vcs {
            }
        }
    })
    return buildType
}
