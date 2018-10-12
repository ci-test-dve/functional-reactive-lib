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

    val jdks = listOf(
            "svenruppert/maven-3.5-jdk-openjdk-10",
            "svenruppert/maven-3.5-jdk-openjdk-11",
            "svenruppert/maven-3.5-jdk-oracle-10",
            "svenruppert/maven-3.5-jdk-zulu-10",
            "svenruppert/maven-3.5-jdk-zulu-11"
    )

    val dockerMavenBuildTemplate = Template {
        id("MavenDocker")
        name = "MavenDockerBuild"

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            maven {
                goals = "%mavenGoals%"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
                mavenVersion = defaultProvidedVersion()
                dockerImage = "%dockerImageName%"
                param("teamcity.tool.jacoco", "%teamcity.tool.jacoco.DEFAULT%")
            }
        }

        triggers {
            vcs {
            }
        }
    }

    template(dockerMavenBuildTemplate)

    jdks.forEach {
        buildType(
                createBuild(jdk = it,
                        template = dockerMavenBuildTemplate,
                        prefix = "build",
                        mavenGoals = "clean install"))
    }

    val mutationTests = createBuild(
            "svenruppert/maven-3.5-jdk-openjdk-10",
            dockerMavenBuildTemplate,
            "mutation",
            "mvn clean package org.pitest:pitest-maven:mutationCoverage"
    )

    buildType(mutationTests)
}


fun createBuild(jdk: String, template: Template, prefix: String, mavenGoals: String): BuildType {
    return BuildType {
        this.name = "$mavenGoals - $jdk"

        this.id = RelativeId(relativeId = (prefix + jdk.substringAfter("/"))
                .replace(
                        oldValue = " ",
                        newValue = ""
                )
                .replace(
                        oldValue = "/",
                        newValue = "_"
                ).replace(
                        oldValue = "-",
                        newValue = "_")
                .replace(
                        oldValue = ".",
                        newValue = "_"))
        templates(template)
        params {
            param("dockerImageName", jdk)
            param("mavenGoals", mavenGoals)
        }
    }
}

