import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("jacoco")
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun prop(name: String): String = localProps.getProperty(name, "")

val openAiApiKey: String = prop("OPENAI_API_KEY")
val supabaseUrl: String = prop("SUPABASE_URL")
val supabaseAnonKey: String = prop("SUPABASE_ANON_KEY")

android {
    namespace = "ua.edu.quizapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "ua.edu.quizapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${supabaseUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${supabaseAnonKey.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"${openAiApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val coveredClassIncludes = listOf(
        "**/ua/edu/quizapp/ui/model/QuestionOptionParserKt.class",
        "**/ua/edu/quizapp/ui/screens/quizplay/AdaptiveQuizEngine.class"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        include(coveredClassIncludes)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("**/*.exec", "**/*.ec")
    })
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    dependsOn("jacocoTestReport")

    val coveredClassIncludes = listOf(
        "**/ua/edu/quizapp/ui/model/QuestionOptionParserKt.class",
        "**/ua/edu/quizapp/ui/screens/quizplay/AdaptiveQuizEngine.class"
    )
    val debugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        include(coveredClassIncludes)
    }

    classDirectories.setFrom(files(debugTree))
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("**/*.exec", "**/*.ec")
    })

    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

dependencies {
    val roomVersion = "2.6.0"
    val navVersion = "2.7.5"

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
