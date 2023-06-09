import com.squareup.workflow1.buildsrc.kotlinCommonSettings

plugins {
  kotlin("multiplatform")
}

extensions.getByType(JavaPluginExtension::class).apply {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
  sourceSets {
    all {
      languageSettings.apply {
        optIn("kotlin.RequiresOptIn")
      }
    }
  }
}

project.kotlinCommonSettings(bomConfigurationName = "commonMainImplementation")
