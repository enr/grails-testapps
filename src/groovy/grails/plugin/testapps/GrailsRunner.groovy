package grails.plugin.testapps

@SuppressWarnings(['Println', 'ThrowException'])
class GrailsRunner {

    private static final boolean OS_WIN = System.getProperty("os.name").toLowerCase().contains("windows")

    String grailsHome
    String dotGrails
    String grailsVersion

    def ant

    GrailsRunner() {
        this(".", ".")
    }

    GrailsRunner(String grailsHome, String dotGrails) {
        this.grailsHome = grailsHome
        this.dotGrails = dotGrails
    }

    /*
     * If true, grailsw will be used when available.
     */
    boolean grailsw = false

    void privilegeGrailsw(boolean privilege) {
        grailsw = privilege
    }

    GrailsProject createApp(String baseDir, String appName) {
        execute(baseDir, 'create-app', [appName])
        String appBaseDir = "${baseDir}/${appName}"
        // create-app exits with 0 in some error case (ie JAVA_HOME not set)
        // so we need additional checks
        def applicationProperties = new File(appBaseDir, 'application.properties')
        if (!applicationProperties.exists()) {
            throw new Exception("grails create-app ${appName} fail")
        }
        return new GrailsProject(appBaseDir)
    }

    String grailsBin(boolean grailswAllowed) {
        def ext = OS_WIN ? '.bat' : ''
        if (grailswAllowed && grailsw) {
            println "grailswAllowed && grailsw"
            def pre = OS_WIN ? '' : './'
            return "${pre}grailsw${ext}"
        }
        if (!grailswAllowed && grailsw) {
            println " ! grailswAllowed && grailsw"
            String wg = Paths.wrapperGrails(this.grailsVersion)
            return "${wg}${ext}"
        }
        return Paths.normalizePath("${grailsHome}/bin/grails${ext}")
    }

    @Override
    public String toString() {
        return "${this.getClass().getName()} ${grailsHome} ${grailsVersion} grailsw=${grailsw}"
    }

    public void setGrailsVersion(String version) {
        this.grailsVersion = version
    }

    public void setAnt(ant) {
        this.ant = ant
    }

    /*
    public void _execute(String dir, String action, extraArgs = []) {
        boolean grailswAllowed = !['create-app', 'create-plugin'].contains(action)
        String exe = grailsBin(grailswAllowed)
        def args = [exe, action]
        extraArgs.each { args << it.toString() }
        args << '-plain-output'
        args << '--stacktrace'
        println " > cd ${dir}"
        println " > ${args.join(' ')}"
        ProcessBuilder processBuilder = new ProcessBuilder(args)
        String javaHome = System.getenv('JAVA_HOME')
        // Create an environment (shell variables)
        Map env = processBuilder.environment()
        // To start a process with an explicit set of environment variables, first call Map.clear() before adding environment variables. 
        env.clear()
        env.put("JAVA_HOME", javaHome)
        env.put("GRAILS_HOME", grailsHome)
        env.put("GRAILS_AGENT_CACHE_DIR", "${dotGrails}/agent_cache")
        processBuilder.directory(new File(dir))
        Process process = processBuilder.start()
        String s = null
        StringBuilder output = new StringBuilder()
        BufferedReader input = new BufferedReader(new InputStreamReader(process.inputStream))
        while ((s = input.readLine()) != null) {
          output.append(" ${s}\n")
        }
        BufferedReader error = new BufferedReader(new InputStreamReader(process.errorStream))
        while ((s = error.readLine()) != null) {
          output.append(" ${s}\n")
        }
        process.waitFor()
        if (process.exitValue() != 0) {
          println output.toString()
          throw new IllegalStateException("Grails ${action} failed. See above for output.")
        }
    }
    */

    public void execute(String dir, String action, extraArgs = []) {
            
        boolean grailswAllowed = !['create-app', 'create-plugin'].contains(action)
        String exe = grailsBin(grailswAllowed)

        String resultproperty = 'exitCode' + System.currentTimeMillis()
        String outputproperty = 'execOutput' + System.currentTimeMillis()
        println " > cd ${dir}"
        println " > ${exe} ${action} ${extraArgs?.join(' ') ?: ''}"

        ant.exec(    executable: exe,
                    dir: dir,
                    failonerror: false,
                    resultproperty: resultproperty,
                    outputproperty: outputproperty) {
            ant.env key: 'GRAILS_HOME', value: grailsHome
            //ant.arg value: env
            ant.arg value: action
            extraArgs.each { ant.arg value: it }
            ant.arg value: '-plain-output'
            ant.arg value: '--stacktrace'
        }

        println ant.project.getProperty(outputproperty)

        int exitCode = ant.project.getProperty(resultproperty) as Integer
        if (exitCode && !ignoreFailure) {
            exit exitCode
        }
    }
}
