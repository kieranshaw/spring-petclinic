import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
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

project {
    buildType(Build)
    buildType(IntegrationTest)
    buildType(PerformanceTest)
    buildType(Deploy)
}

object Build : BuildType({
    name = "Build"
    artifactRules = "target/*.jar"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean package"
            mavenVersion = defaultProvidedVersion()
        }
        script {
            scriptContent = """
        dir
        dir target
    """.trimIndent()
        }
    }
})

object IntegrationTest : BuildType({
    name = "Test - Integration"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            watchChangesInDependencies = true
        }
    }

    steps {
        maven {
            goals = "clean integration-test -DskipTests"
            mavenVersion = defaultProvidedVersion()
        }
    }

    dependencies {
        snapshot(Build) {}
    }

})

object PerformanceTest : BuildType({
    name = "Test - Performance"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            watchChangesInDependencies = true
        }
    }

    steps {
        maven {
            goals = "clean test"
            mavenVersion = defaultProvidedVersion()
        }
    }

    dependencies {
        snapshot(Build) {}
    }

})

object Deploy : BuildType({
    name = "Deploy - Dev"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            watchChangesInDependencies = true
        }
    }

    steps {
        script {
            scriptContent = "dir"
        }
    }

    dependencies {
        artifacts(Build) {
            buildRule = sameChainOrLastFinished()
            artifactRules = "**/*.jar"
        }
        snapshot(IntegrationTest) {}
        snapshot(PerformanceTest) {}
    }

})
