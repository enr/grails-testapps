package grails.plugin.testapps

import spock.lang.*

/**
 * Specification for GrailsRunner.
 */
class GrailsRunnerSpec extends Specification {

	private static final boolean OS_WIN = System.getProperty("os.name").toLowerCase().contains("windows")

	private static final String USER_HOME = System.getProperty("user.home")

	GrailsRunner grails

	@IgnoreIf({ !OS_WIN })
    def "grails executable resolution on win box"() {
        given:
        grails = new GrailsRunner(grailsHome, grailsHome)
        grails.privilegeGrailsw(grailsw)

        and:
        def bin = grails.grailsBin(grailswAllowed)

        expect:
        bin.contains(grailsBinPath)

        where:
        grailsHome                                  | grailsw   | grailswAllowed    | grailsBinPath
        /C:\Documents and settings\grails\2.3.3/    | true      | true              | "grailsw"
        /C:\Documents and settings\grails\2.3.3/    | false     | false             | "C:/Documents and settings/grails/2.3.3/bin/grails.bat"
        /C:\Documents and settings\grails\2.3.3/    | false     | false             | "C:/Documents and settings/grails/2.3.3/bin/grails.bat"
        /C:\Documents and settings\grails\2.3.3/    | false     | false             | "C:/Documents and settings/grails/2.3.3/bin/grails.bat"
    }

	@IgnoreIf({ OS_WIN })
    def "grails executable resolution on nix box"() {
        given:
        grails = new GrailsRunner(grailsHome, grailsHome)
        grails.privilegeGrailsw(grailsw)
        grails.setGrailsVersion(grailsVersion)

        and:
        def bin = grails.grailsBin(grailswAllowed)

        expect:
        bin.contains(grailsBinPath)

        where:
        grailsHome              | grailsVersion    | grailsw   | grailswAllowed    | grailsBinPath
        "/opt/grails/current"   | '2.3.8'          | true      | true              | "./grailsw"
        "/opt/grails/current"   | '2.3.8'          | true      | false             | "${USER_HOME}/.grails/wrapper/2.3.8/grails-2.3.8/bin/grails"
        null                    | '2.3.8'          | true      | false             | "${USER_HOME}/.grails/wrapper/2.3.8/grails-2.3.8/bin/grails"
    }

    def "grails executable resolution multiplat"() {
        given:
        grails = new GrailsRunner(grailsHome, grailsHome)
        grails.privilegeGrailsw(grailsw)
        grails.setGrailsVersion('2.3.6')

        and:
        def bin = grails.grailsBin(grailswAllowed)

        expect:
        bin.contains(grailsBinPath)

        where:
        grailsHome                                  | grailsw   | grailswAllowed    | grailsBinPath
        "/opt/grails/current"                       | true      | true              | "grailsw"
        "/opt/grails/current"                       | true      | false             | "${USER_HOME}/.grails/wrapper/2.3.6/grails-2.3.6/bin/grails".replace(File.separator, '/')
        "/opt/grails/current"                       | false     | true              | "/opt/grails/current/bin/grails"
    }
}  