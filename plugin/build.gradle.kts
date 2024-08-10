import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val pluginName = "GodotAndroidPluginMagicLink"

val pluginPackageName = "org.godotengine.plugin.android.magiclink"

android {
    namespace = pluginPackageName
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        setProperty("archivesBaseName", pluginName)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.godotengine:godot:4.2.1.stable")
    // TODO: Additional dependencies should be added to export_plugin.gd as well.
    implementation("link.magic:magic-android:10.6.0")
    implementation("link.magic:magic-ext-oauth:5.0.1")
    implementation("link.magic:magic-ext-oidc:2.0.4")
    implementation("org.web3j:core:4.8.8-android")
    implementation("org.web3j:geth:4.8.8-android")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}

// BUILD TASKS DEFINITION
val copyDebugAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated debug AAR binary to the plugin's addons directory"
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("demo/addons/$pluginName/bin/debug")
}

val copyReleaseAARToDemoAddons by tasks.registering(Copy::class) {
    description = "Copies the generated release AAR binary to the plugin's addons directory"
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("demo/addons/$pluginName/bin/release")
}

val cleanDemoAddons by tasks.registering(Delete::class) {
    delete("demo/addons/$pluginName")
}

val copyAddonsToDemo by tasks.registering(Copy::class) {
    description = "Copies the export scripts templates to the plugin's addons directory"
    dependsOn(cleanDemoAddons)
    mustRunAfter(copyDebugAARToDemoAddons, copyReleaseAARToDemoAddons)

    from("export_scripts_template")
    into("demo/addons/$pluginName")
}

val copyToParentAddons by tasks.registering(Copy::class) {
    description = "Copies the contents of demo/addons/$pluginName to ../addons/$pluginName"
    dependsOn(copyAddonsToDemo, copyDebugAARToDemoAddons, copyReleaseAARToDemoAddons)
    mustRunAfter(copyAddonsToDemo)

    from("demo/addons/$pluginName")
    into("../addons/$pluginName")
}

tasks.named("assemble").configure {
    finalizedBy(copyAddonsToDemo)
}

tasks.named<Delete>("clean").configure {
    dependsOn(cleanDemoAddons)
}

// Ensure copyToParentAddons runs after copyAddonsToDemo
tasks.named("copyAddonsToDemo").configure {
    finalizedBy(copyToParentAddons)
}

// Ensure that copyDebugAARToDemoAddons and copyReleaseAARToDemoAddons must run after cleanDemoAddons
tasks.named("cleanDemoAddons").configure {
    finalizedBy(copyDebugAARToDemoAddons, copyReleaseAARToDemoAddons)
}
