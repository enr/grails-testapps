
import sample.testapp.*

class BootStrap {

    def grailsApplication
    
    def init = { servletContext ->
        log.info('-'*60);
        log.info "S2 user domain class = ${grailsApplication.config.grails.plugin.springsecurity.userLookup.userDomainClassName}"
        log.info "S2 user role join = ${grailsApplication.config.grails.plugin.springsecurity.userLookup.authorityJoinClassName}"
        log.info "S2 authority class = ${grailsApplication.config.grails.plugin.springsecurity.authority.className}"

        def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
        def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

        def adminName = 'demoadmin'
        def adminUser = User.findByUsername(adminName) ?: new User(
                username: adminName,
                password: adminName,
                enabled: true).save(failOnError: true)

                if (!adminUser.authorities.contains(adminRole)) {
            UserRole.create adminUser, adminRole
        }
        log.info "Created admin user: ${adminUser.username}/${adminUser.username}"

        def regularUserName = 'demouser'
        def regularUser = User.findByUsername(regularUserName) ?: new User(
                username: regularUserName,
                password: regularUserName,
                enabled: true).save(failOnError: true)

        if (!regularUser.authorities.contains(userRole)) {
            UserRole.create regularUser, userRole
        }
        log.info "Created regular user: ${regularUser.username}/${regularUser.username}"
        log.info('-'*60);
    }

    def destroy = {
    }
}
