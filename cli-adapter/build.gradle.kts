plugins {
    application
}

dependencies {
    implementation(project(":domain"))
    implementation("com.github.ajalt.clikt:clikt:3.5.0")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
}

application {
    mainClass.set("org.example.krawler.cli.MainKt")
    applicationName = "krawler"
}

