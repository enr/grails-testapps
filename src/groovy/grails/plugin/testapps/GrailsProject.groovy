package grails.plugin.testapps

class GrailsProject {

    String basedir

    def paths

    /*
     * List of allowed log levels.
     */
    private static final List<String> LOG_LEVELS = ['debug', 'info', 'warn']

    public GrailsProject() {
        this(".")
    }

    public GrailsProject(String basedir) {
        this.basedir = basedir
        paths = new Paths()
    }

    boolean isPlugin() {
        return (paths.pluginDescriptorInDirectory(basedir))
    }

    public void installPlugin(String pluginCoords, String scope) {
        File buildConfig = new File(basedir, paths.grailsBuildConfig())
        String contents = buildConfig.text
        contents = contents.replace('plugins {', """plugins {
${scope} '${pluginCoords}'
""")
        buildConfig.withWriter { it.writeLine contents }
    }

    public void addLogging(String level, List<String> packages) {
        String logConfigurationBlock = loggingBlock(level, packages)
        if (!logConfigurationBlock) {
            return
        }
        File config = new File(basedir, paths.grailsConfig())  
        String contents = config.text
        contents = contents.replace('log4j = {', "log4j = { ${logConfigurationBlock}\n")
        config.withWriter { it.writeLine contents }
    }

    String loggingBlock(String level, List<String> packages) {
        if (!LOG_LEVELS.contains(level)) {
            throw new IllegalArgumentException("Error configuring log... Level '${level}' not available")
        }
        if (!packages) {
            return ''
        }
        String infoLogs = ""
        packages.eachWithIndex() { pkg, i ->
            def pre = (i == 0) ? "" : ", "
            infoLogs += "${pre}'${pkg}'"
        }
        String logConfigurationBlock = "${level} ${infoLogs}"
        return logConfigurationBlock
    }

    public void editBuildConfig(String mavenLocalRepo) {
        File buildConfig = new File(basedir, paths.grailsBuildConfig())  
        String contents = editedBuildConfigText(buildConfig.text, mavenLocalRepo)
        buildConfig.withWriter { it.writeLine contents }
    }

    String editedBuildConfigText(String original, String mavenLocalRepo) {
        if (!original) {
            throw new IllegalArgumentException("Error editing BuildConfig... No content found")
        }
        // set build dirs
        String contents = original.replace('grails.project.class.dir = "target/classes"', '')
        contents = contents.replace('grails.project.test.class.dir = "target/test-classes"', '')
        contents = contents.replace('grails.project.test.reports.dir = "target/test-reports"', '')
        
        // disable fork
        contents = contents.replace('grails.project.fork', 'grails.project.fork_DISABLED')
        
        // setup mavenLocal
        contents = contents.replace('mavenLocal()', "\nmavenLocal('${mavenLocalRepo}')")

        // add repositories
        contents = contents.replace('//mavenRepo "http://repository.jboss.com/maven2/"', '''
mavenRepo "http://repo.spring.io/release"
mavenRepo "http://repo.spring.io/external"
mavenRepo "http://repo.spring.io/milestone"
mavenRepo "http://snapshots.repository.codehaus.org"
mavenRepo "http://repository.codehaus.org"
mavenRepo "http://download.java.net/maven/2/"
mavenRepo "http://repository.jboss.com/maven2/"
''')

        // remove controversial plugins
        //contents = contents.replace('runtime ":database-migration:', '//database-migration')
        //contents = contents.replace('runtime ":hibernate:', '//hibernate')
        //contents = contents.replace('runtime ":hibernate4:', '//hibernate4')
        return contents
    }

    public void installDependency(String dep, String scope) {
        File buildConfig = new File(basedir, paths.grailsBuildConfig())
        String contents = buildConfig.text
        contents = contents.replace('dependencies {', """dependencies {
${scope} '${dep}'
    """)
        buildConfig.withWriter { it.writeLine contents }
    }

    @Override
    public String toString() {
        return "${this.getClass().getName()} ${basedir}"
    }
}
