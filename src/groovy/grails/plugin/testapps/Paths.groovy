package grails.plugin.testapps

/**
 * Utility class pertaining paths.
 */
class Paths {

    public static final String GRAILS_WRAPPER_BASE = "${System.getProperty('user.home')}/.grails/wrapper"
    public static final String MAVEN_LOCAL_REPO_DEFAULT = "${System.getProperty('user.home')}/.m2/repository"
    public static final String MAVEN_SETTINGS_FILE = "${System.getProperty('user.home')}/.m2/settings.xml"

    static String mavenLocalRepoDefault() {
        return normalizePath(MAVEN_LOCAL_REPO_DEFAULT)
    }

    String mavenLocalRepo() {
        return mavenLocalRepoFromSettings(MAVEN_SETTINGS_FILE)
    }

    String mavenLocalRepoFromSettings(String mavenSettingsPath) {
        def settingsFile = new File(mavenSettingsPath)
        if (settingsFile.exists()) {
            def settingsXml = new XmlParser().parse(settingsFile)
            if (settingsXml.localRepository.size() == 1) {
                return Paths.normalizePath(settingsXml.localRepository[0].text())
            }
        }
        return Paths.normalizePath(MAVEN_LOCAL_REPO_DEFAULT)
    }

    String wrapperGrailsHome(String version) {
        return Paths.normalizePath("${GRAILS_WRAPPER_BASE}/${version}/grails-${version}")
    }

    File pluginDescriptorInDirectory(String basedir) {
        File pluginFile
        new File("${basedir}").eachFile {
            if (it.name.endsWith("GrailsPlugin.groovy")) {
                pluginFile = it
            }
        }
        return pluginFile
    }

    public static String wrapperGrails(String version) {
        Paths.normalizePath("${GRAILS_WRAPPER_BASE}/${version}/grails-${version}/bin/grails")
    }

    String grailsConfig() {
        return "grails-app/conf/Config.groovy"
    }

    String grailsBuildConfig() {
        return "grails-app/conf/BuildConfig.groovy"
    }

    // fix win backslash
    public static String normalizePath(String path) {
        return (path ? path.replaceAll("\\\\", "/") : "")
    }
}
