
includeTargets << grailsScript('_GrailsBootstrap')
includeTargets << grailsScript("_GrailsArgParsing")

import grails.util.GrailsUtil
import grails.util.GrailsNameUtils

isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
userHomeDir = System.getProperty("user.home")

mavenLocalRepo = null

// The current project where testapps plugin is installed in.
project = null

// The grails.plugin.testapps.Paths component.
paths = null

// The grails.plugin.testapps.GrailsRunner component.
grails = null

// Base directory for testapps template files:
// will be set to `pwd` + testapps
projectfiles = null

// --- Plugin under test properties
// Install the plugin in test apps?
pluginInstall = true
pluginScope = null
// Version: if not set will be taken from plugin descriptor
pluginVersion = null
// Group: if not set will be "org.grails.plugins"
pluginGroup = null

// --- Testapp
// Instance of grails.plugin.testapps.GrailsProject
currentTestapp = null
// the identifier used in testapps.config name
appId = null
// the full app name, something similar to testapps-$appId-$timestamp
appName = null

// --- Grails
// Default Grails home; will be set to the current system GRAILS_HOME env var
bootstrapGrailsHome = null
grailsHome = null
// taken from configuration or $HOME/.grails
dotGrails = null
// Directory where test apps will be created in.
projectDir = null
plugins = [:]
logDebugPackages = []
logInfoPackages = []
dependencies = [:]
scripts = []
customRepos = []

// use grailsw when possible?
grailsw = false

testprojectRoot = null
deleteAll = false
timestamp = true

customConfig = null

target(createTestApps: 'Creates test apps') {
    depends(parseArguments)

    def grailsVersion = GrailsUtil.grailsVersion

	project = classLoader.loadClass('grails.plugin.testapps.GrailsProject').newInstance()
    paths = classLoader.loadClass('grails.plugin.testapps.Paths').newInstance()

    bootstrapGrailsHome = System.getenv('GRAILS_HOME') //?: paths.wrapperGrailsHome(grailsVersion)

    def configFilePath = argsMap.params[0] ?: 'testapps.config.groovy'
    grailsConsole.updateStatus "Using configFilePath ${configFilePath}"
        
    def configFile = new File(basedir, configFilePath)

    if (!configFile.exists()) {
        die "${configFile.path} not found"
    }
    mavenLocalRepo = getMavenLocalRepository()

    
    // TODO: check con versione in application.properties del plugin
    // evenutally: grailsUpgrade()
    grailsConsole.updateStatus "Using Grails ${grailsVersion} ${bootstrapGrailsHome}"

	projectfiles = new File(basedir, 'testapps')

    new ConfigSlurper().parse(configFile.text).each { name, config ->
        grailsConsole.updateStatus "\nCreating app based on configuration ${name}"
        //": ${config.flatten()}\n"

        init name, config
        cleanPaths()
        currentTestapp = createApp()
        addConfigLocations()
        editBuildConfig()
        installPlugins()
        installDependencies()
        addLogs()
        addCustomConfig()
        compileApp()
        runScripts()
        copySampleFiles()
    }
}

private void init(String name, config) {
    // Current project
    def isPlugin = project.isPlugin()
    if (isPlugin) {
        def pluginInfo = pluginSettings.getPluginInfo(basedir)
        pluginVersion = config.pluginVersion  ?: pluginInfo.version
        pluginGroup = resolvePluginGroupId()
        grailsConsole.updateStatus "Plugin Under Test: Group=${pluginGroup} Name=${pluginInfo.name} Version=${pluginVersion}"
    }
    pluginScope = config.pluginScope ?: 'compile'
    pluginInstall = (config.pluginInstall == false) ? false : (isPlugin && pluginInstall)
    grailsConsole.updateStatus "Install Plugin in test apps? ${pluginInstall}"

    // Grails
    grailsHome = config.grailsHome ?: bootstrapGrailsHome

    grailsw = config.grailsw ?: false
    grailsConsole.updateStatus "Using Grails Wrapper? ${grailsw}"
    if (".." == "${grailsHome}" && grailsw) {
        grailsHome = paths.wrapperGrailsHome(GrailsUtil.grailsVersion)
    }
    grailsConsole.updateStatus "Using Grails home ${grailsHome}"
    if (!new File(grailsHome).exists()) {
        die "Grails home ${grailsHome} not found"
    }

    dotGrails = config.dotGrails ?: userHomeDir + File.separator + '.grails'
    grailsConsole.updateStatus "Using .Grails ${dotGrails}"
    grails = classLoader.loadClass('grails.plugin.testapps.GrailsRunner').newInstance(grailsHome, dotGrails)
    grails.privilegeGrailsw(grailsw)
    // WARN: if you use grailsw to run this script and an external Grails installation to run subprocess,
    // this version (reflectiong the grailsw version) could be different from the actual used version.
    grails.setGrailsVersion(GrailsUtil.grailsVersion)
    grails.setAnt(ant)
    grailsConsole.updateStatus "GrailsRunner: ${grails}"

    // Current test app
    def appNameSuffix = (config.timestamp == false) ? '' : "-${System.currentTimeMillis()}"
    projectDir = config.projectDir ?: 'target'
    appId = name
    appName = "testapp-${name}${appNameSuffix}".toString()
    testprojectRoot = "${projectDir}/${appName}"

    plugins = config.plugins
    dependencies = config.dependencies
    scripts = config.scripts
    customRepos = config.customRepos
    customConfig = config.customConfig ?: ''
    logDebugPackages = config.log.debug ?: []
    logInfoPackages = config.log.info ?: []
}

private String resolvePluginGroupId() {
    File descriptor = getPluginFile()
    def pluginType = descriptor.name - '.groovy'
    def plugin = classLoader.loadClass(pluginType).newInstance()
    def pluginGroup = 'org.grails.plugins'
    if (plugin.metaClass.hasProperty(plugin, 'group')) {
        pluginGroup = plugin.group
    }
    if (plugin.metaClass.hasProperty(plugin, 'groupId')) {
        pluginGroup = plugin.groupId
    }
    return pluginGroup
}

private File getPluginFile() {
    return paths.pluginDescriptorInDirectory(basedir)
}

private void cleanPaths() {
    deleteDir testprojectRoot
    deleteDir "${dotGrails}/${GrailsUtil.grailsVersion}/projects/${appName}"
}

def createApp() {
    grailsConsole.updateStatus "Creating app ${testprojectRoot}"
    ant.mkdir dir: projectDir
    return grails.createApp(projectDir, appName)
}

private void addConfigLocations() {
    appendToConfig('grails.config.locations = ["file:./testapps-config.groovy"]')
}

private void addCustomConfig() {
    appendToConfig(customConfig)
}

private void addLogs() {
    currentTestapp.addLogging('debug', logDebugPackages)
    currentTestapp.addLogging('info', logInfoPackages)
}

private void editBuildConfig() {
    currentTestapp.editBuildConfig(mavenLocalRepo, customRepos)
}

private void copySampleFiles() {
    if (!projectfiles.exists()) {
        grailsConsole.updateStatus "${projectfiles.path} not found. Skip copying"
        return
    }
    copyDirToTestapp("${projectfiles.path}/all")
    copyDirToTestapp("${projectfiles.path}/${appId}")
}

// FileUtils.copyDirectory(src, dest)
private void copyDirToTestapp(String path) {
    def appTemplatesDir = new File(path)
    if (appTemplatesDir.exists()) {
        ant.copy(todir: testprojectRoot, failonerror: true, overwrite:true) {
            fileset(dir: "${path}", includes: "** /**")
        }
        grailsConsole.updateStatus "Copy ${path} to ${testprojectRoot}"
    } else {
        grailsConsole.updateStatus "App templates dir ${path} not found. Skip copying templates"
    }
}

private void appendToConfig(String configuration) {
    def contents = "\n// added from create-testapps script\n${configuration} \n"
    File configFile = new File(testprojectRoot, paths.grailsConfig())
    configFile.withWriterAppend { it.writeLine contents }
}

private void installPlugins() {
    ['build', 'compile', 'runtime', 'test'].each { scope ->
        for (plugin in plugins[scope]) {
            grailsConsole.updateStatus "Installing plugin <${plugin}> in scope <${scope}>"
            installPlugin(plugin, scope)
        }
    }
    if (pluginInstall) {
        installPluginUnderTest()
    } else {
        grailsConsole.updateStatus "Skipping plugin installation"
    }
}

private void installPluginUnderTest() {
    grailsConsole.updateStatus "Installing plugin via Maven. Repo: ${mavenLocalRepo}"
    // workaround: sometimes maven-install script is not found
    grails.execute(basedir, 'compile')
    grails.execute(basedir, 'maven-install')
    String pluginCoords = "${pluginGroup}:${getCurrentPluginName()}:${pluginVersion}"
    installPlugin(pluginCoords, pluginScope)
}

private String getCurrentPluginName() {
    if (!project.isPlugin()) {
        ant.fail("getCurrentPluginName: The project is not a plugin")
    }
    def pluginFile = paths.pluginDescriptorInDirectory(basedir)
    def pluginName = GrailsNameUtils.getPluginName(pluginFile.name)
    return pluginName
}

private void installPlugin(String pluginCoords, scope) {
    currentTestapp.installPlugin(pluginCoords, scope)
}

private void installDependencies() {
    ['compile', 'runtime', 'test'].each { scope ->
        for (dep in dependencies[scope]) {
            grailsConsole.updateStatus "Installing dependency <${dep}> in scope <${scope}>"
            currentTestapp.installDependency(dep, scope)
        }
    }
}

private void compileApp() {
    grailsConsole.updateStatus "Compiling app"
    def args = ['-non-interactive']
    grails.execute(testprojectRoot, 'compile', args)
}

private void runScripts() {
    for (script in scripts) {
        grailsConsole.updateStatus "Executing script ${script.name} ${script.args}"
        grails.execute(testprojectRoot, script.name, script.args)
    }
}

private void deleteDir(String path) {
    if (new File(path).exists() && !deleteAll) {
        String code = "confirm.delete.$path"
        ant.input message: "${path} exists, ok to delete?", addproperty: code, validargs: 'y,n,a'
        def result = ant.antProject.properties[code]
        if ('a'.equalsIgnoreCase(result)) {
            deleteAll = true
        }
        else if (!'y'.equalsIgnoreCase(result)) {
            grailsConsole.updateStatus "\nNot deleting $path"
            exit 1
        }
    }
    ant.delete dir: path
}

private String getMavenLocalRepository() {
    return paths.mavenLocalRepo()
}

private void die(String message) {
    grailsConsole.error message
    exit 1
}

setDefaultTarget 'createTestApps'
