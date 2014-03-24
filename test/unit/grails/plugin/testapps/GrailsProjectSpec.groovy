package grails.plugin.testapps

import spock.lang.*

class GrailsProjectSpec extends Specification {

	GrailsProject project

    def "isPlugin should return true if descriptor is found"() {
    	given:
    	project = new GrailsProject()
        def paths = [pluginDescriptorInDirectory:{ basedir -> descriptor }]
        project.paths = paths

    	and:
    	def res = project.isPlugin()

        expect:
        res == isPlugin

        where:
        descriptor                          | isPlugin
        new File("TestGrailsPlugin.groovy") | true
        null                                | false
    }

    def "log block configuration"() {
        given:
        project = new GrailsProject()

        and:
        def res = project.loggingBlock(level, packages) 

        expect:
        res == block

        where:
        level   | packages          | block
        'info'  | []                | ''
        'debug' | ['a.b', 'c.d.e']  | "debug 'a.b', 'c.d.e'"
        'info'  | ['a.b', 'c.d.e']  | "info 'a.b', 'c.d.e'"
    }

    def "log level error"() {
        given:
        project = new GrailsProject()

        when:
        def res = project.loggingBlock('unknown', ['a', 'b']) 

        then:
        thrown(IllegalArgumentException)
    }

    def "edit BuildConfig"() {
        given:
        project = new GrailsProject()
        def original = new File(buildConfigPath).text
        def expected = new File(expectedBuildConfigPath).text

        and:
        def res = project.editedBuildConfigText(original, '/opt/maven/repo')

        expect:
        res == expected

        where:
        buildConfigPath                                 | expectedBuildConfigPath
        "test/data/build_config/BuildConfig233.groovy"  | "test/data/build_config/BuildConfig233_out.groovy"
    }

}  