import org.gradle.api.JavaVersion

object Build {
    const val COMPILE_SDK = 34
    const val MIN_SDK = 21
    val sourceCompatibility = JavaVersion.VERSION_11
    val targetCompatibility = JavaVersion.VERSION_11
}
