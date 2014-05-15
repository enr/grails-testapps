
if(System.getenv('TRAVIS_BRANCH')) {
    grails.project.repos.grailsCentral.username = System.getenv("GRAILS_CENTRAL_USERNAME")
    grails.project.repos.grailsCentral.password = System.getenv("GRAILS_CENTRAL_PASSWORD")
}

grails.project.work.dir = 'target'

grails.project.fork_disabled = [
    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

    inherits("global") { }

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    plugins {
        build(":release:3.0.1",
              ":rest-client-builder:1.0.3") {
            export = false
        }
        test ":code-coverage:1.2.7"
        compile ":codenarc:0.21"
    }
}

codenarc {
    reports = {
        TestappsXmlReport('xml') {
            outputFile = 'target/CodeNarc-Report.xml'
            title = 'Testapps plugin CodeNarc Report'
        }
        TestappsHtmlReport('html') {
            outputFile = 'target/CodeNarc-Report.html'
            title = 'Testapps plugin CodeNarc Report'
        }
    }
    ruleSetFiles='file:grails-app/conf/TestappsCodeNarcRuleSet.groovy'
    maxPriority1Violations = 0
    maxPriority2Violations = 0
    maxPriority3Violations = 0
}

coverage {
	exclusions = ['**/CodeNarcRuleSet*']
}
