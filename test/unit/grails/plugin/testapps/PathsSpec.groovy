package grails.plugin.testapps

import spock.lang.*

/**
 * Specification for Paths.
 */
class PathsSpec extends Specification {

	Paths paths

    def "mavenLocalRepoFromSettings"() {
    	given:
    	paths = new Paths()

    	and:
    	def res = paths.mavenLocalRepoFromSettings(settingsPath)

        expect:
        res == localRepo

        where:
        settingsPath                            | localRepo
        "test/data/maven_settings/norepo.xml"   | Paths.mavenLocalRepoDefault()
        "test/data/maven_settings/standard.xml" | "/opt/maven/repository"
        "test/data/maven_settings/windows.xml"  | "C:/opt/maven/repository"
    }
}  