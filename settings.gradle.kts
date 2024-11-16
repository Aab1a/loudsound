pluginManagement {
    repositories {
        maven { url=uri ("https://jitpack.io") }
        maven { url=uri ("https://maven.aliyun.com/repository/releases") }
//        maven { url=uri ("https://maven.aliyun.com/repository/jcenter") }
        maven { url=uri ("https://maven.aliyun.com/repository/google") }
        maven { url=uri ("https://maven.aliyun.com/repository/central") }
        maven { url=uri ("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url=uri ("https://maven.aliyun.com/repository/public") }
        maven { url=uri ("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url=uri ("https://mvn.0110.be/releases") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url=uri ("https://jitpack.io") }
        maven { url=uri ("https://maven.aliyun.com/repository/releases") }
//        maven { url=uri ("https://maven.aliyun.com/repository/jcenter") }
        maven { url=uri ("https://maven.aliyun.com/repository/google") }
        maven { url=uri ("https://maven.aliyun.com/repository/central") }
        maven { url=uri ("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url=uri ("https://maven.aliyun.com/repository/public") }
        maven { url=uri ( "https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url=uri ("https://mvn.0110.be/releases") }
        google()
        mavenCentral()
    }
}


rootProject.name = "loudsound"
include(":app")
 