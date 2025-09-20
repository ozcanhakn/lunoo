import java.net.URI

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()


    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://jitpack.io") } // Burada `url = uri(...)` şeklinde kullanmanız gerekiyor
        mavenCentral()
        maven { url = URI.create ("https://storage.zego.im/maven") }   // <- Add this line.
    }
}

rootProject.name = "Lumoo"
include(":app")
