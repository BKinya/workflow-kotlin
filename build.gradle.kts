import com.squareup.workflow1.buildsrc.shardConnectedCheckTasks
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import java.net.URL

buildscript {
  dependencies {
    classpath(libs.android.gradle.plugin)
    classpath(libs.dokka.gradle.plugin)
    classpath(libs.kotlin.serialization.gradle.plugin)
    classpath(libs.kotlinx.binaryCompatibility.gradle.plugin)
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.google.ksp)
    classpath(libs.vanniktech.publish)
  }

  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
    // For binary compatibility validator.
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
  }
}

plugins {
  base
  id("artifacts-check")
  id("dependency-guard")
  alias(libs.plugins.ktlint)
  alias(libs.plugins.compose.compiler) apply false
}

shardConnectedCheckTasks(project)

apply(from = rootProject.file(".buildscript/binary-validation.gradle"))

// This plugin needs to be applied to the root projects for the dokkaGfmCollector task we use to
// generate the documentation site.
apply(plugin = "org.jetbrains.dokka")

// Configuration that applies to all dokka tasks, both those used for generating javadoc artifacts
// and the documentation site.
subprojects {
  tasks.withType<AbstractDokkaLeafTask> {

    // This is the displayed name for the module, like in the Html sidebar.
    //   artifact id: workflow-internal-testing-utils
    //          path: internal-testing-utils
    moduleName.set(
      provider {
        findProperty("POM_ARTIFACT_ID") as? String
          ?: project.path.removePrefix(":")
      }
    )

    dokkaSourceSets.configureEach {

      val dokkaSourceSet = this

      reportUndocumented.set(false)
      skipDeprecated.set(true)

      if (file("src/${dokkaSourceSet.name}").exists()) {

        val readmeFile = file("$projectDir/README.md")
        // If the module has a README, add it to the module's index
        if (readmeFile.exists()) {
          includes.from(readmeFile)
        }

        sourceLink {
          localDirectory.set(file("src/${dokkaSourceSet.name}"))

          val modulePath = projectDir.relativeTo(rootDir).path

          // URL showing where the source code can be accessed through the web browser
          remoteUrl.set(
            @Suppress("ktlint:standard:max-line-length")
            URL(
              "https://github.com/square/workflow-kotlin/blob/main/$modulePath/src/${dokkaSourceSet.name}"
            )
          )
          // Suffix which is used to append the line number to the URL. Use #L for GitHub
          remoteLineSuffix.set("#L")
        }
      }
      perPackageOption {
        // Will match all .internal packages and sub-packages, regardless of module.
        matchingRegex.set(""".*\.internal.*""")
        suppress.set(true)
      }
    }
  }
}

// Publish tasks use the output of Sign tasks, but don't actually declare a dependency upon it,
// which then causes execution optimizations to be disabled.  If this target project has Publish
// tasks, explicitly make them run after Sign.
subprojects {
  tasks.withType(AbstractPublishToMaven::class.java)
    .configureEach { mustRunAfter(tasks.matching { it is Sign }) }

  tasks.withType(Test::class.java)
    .configureEach {
      testLogging {
        // This prints exception messages and stack traces to the log when tests fail. Makes it a
        // lot easier to see what failed in CI. If this gets too noisy, just remove it.
        exceptionFormat = FULL
      }
    }
}

// This task is invoked by the documentation site generator script in the main workflow project (not
// in this repo), which also expects the generated files to be in a specific location. Both the task
// name and destination directory are defined in this script:
// https://github.com/square/workflow/blob/main/deploy_website.sh
tasks.register<Copy>("siteDokka") {
  description = "Generate dokka Html for the documentation site."
  group = "documentation"
  dependsOn(":dokkaHtmlMultiModule")

  // Copy the files instead of configuring a different output directory on the dokka task itself
  // since the default output directories disambiguate between different types of outputs, and our
  // custom directory doesn't.
  from(layout.buildDirectory.file("dokka/htmlMultiModule/workflow"))
  into(layout.buildDirectory.file("dokka/workflow"))
}
