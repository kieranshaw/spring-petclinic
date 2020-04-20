import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

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

version = "2019.2"

val operatingSystems = listOf("Mac OS X", "Windows 10", "Linux")
val jdkVersions = listOf("JDK_18", "JDK_11")

project {
    for (os in operatingSystems) {
        for (jdk in jdkVersions) {
            buildType(Build(os, jdk))
        }
    }
}

class Build(val os: String, val jdk: String) : BuildType({
    id("Build_${os}_${jdk}".toExtId())
    name = "Build ($os, $jdk)"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean test"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.${jdk}%"
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.name", os)
    }
})
