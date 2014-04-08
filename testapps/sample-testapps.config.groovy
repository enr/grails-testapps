
// these vars outside app configuration, are defined only to not repeat settings for every app
// they are all not mandatory

// version for the plugin under test (if any)
//String version = '0.1'

// base dir for different Grails installations
//String grailsHomeRoot = '/opt/grails'

// base dir for .grails dir
//String dotGrailsCommon = '/home/user/.grails'

// path where test apps will be created
//String projectDirCommon = '/opt/grails/testapps'
String projectDirCommon = new File('target').absolutePath

// sample will be part of the name of the test app
// by default the full name will be "testapp-sample-$timestamp"
// the scripts will look for templates in testapps/sample directory
sample {

    // use ./grailsw to run Grails commands
    // commands requiring a plain installation, will be executed using:
    // ${HOME}/.grails/wrapper/grails-${version}/bin/grails
    //grailsw = true

    // if the app doesn't set Grails version and home, it will be created using the standard installation of Grails
    // the Grails version to use
    //grailsVersion = '2.3.3'
    //dotGrails = dotGrailsCommon
    // Grails home is not mandatory.
    // if empty will be set to the value of the env var GRAILS_HOME
    //grailsHome = "${grailsHomeRoot}/grails-${grailsVersion}"
    //grailsHome = 'c:/opt/grails-2.4.0.M1'
    //grailsHome = '/opt/grails/current'

    // version for the plugin under test (if any)
    //pluginVersion = version

    // don't install the plugin under test (in this case the "testapps" plugin)
    pluginInstall = false

    // directory where test apps will be created in
    projectDir = projectDirCommon

    // append timestamp to testapp name?
    // default is true, you have to specify only if you don't want to
    //timestamp = false

    // other plugins to install into the test app
    plugins {
        compile = [':spring-security-core:2.0-RC2', ':spring-security-ui:1.0-RC1', ':greenmail:1.3.4', ':mail:1.0.1']
        runtime = [':jquery-ui:1.10.3', ':famfamfam:1.0.1']
    }

    dependencies {
        compile = ['org.scribe:scribe:1.3.6']
    }

    // custom repositories
    customRepos = ['https://raw.github.com/fernandezpablo85/scribe-java/mvn-repo']
    
    // will be appended to grails-app/conf/Config.groovy
    customConfig = '''

environments {
    production {
        greenmail.disabled = true
        grails.serverURL = "http://www.changeme.com"
    }
    development {
    grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
    grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

'''

    // packages added to Config.groovy in log4j section
    log {
        debug = ['sample.testapp']        
        info = [   'grails.app.conf.BootStrap',
                   'grails.app.filters',
                   'grails.app.dataSource',
                   'grails.app.tagLib',
                   'grails.app.services',
                   'grails.app.controllers',
                   'grails.app.domain']
    }

    // scripts to execute
    scripts = [
        [name:'s2-quickstart', args:['sample.testapp', 'User', 'Role']]
    ]

}

String gvmDir = System.getenv('GVM_DIR')

v240M1 {
    // this app will be created using Grails 2.4.0.M1
    grailsHome = "${gvmDir}/grails/2.4.0.M1"
    pluginInstall = false
    projectDir = projectDirCommon
    plugins {
        compile = [':greenmail:1.3.4', ':mail:1.0.1']
        runtime = [':famfamfam:1.0.1']
    }
}

