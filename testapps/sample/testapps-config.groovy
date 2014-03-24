
// this file is added to config locations.

grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.interceptUrlMap = [
    '/':                ['permitAll'],
    '/index':           ['permitAll'],
    '/index.gsp':       ['permitAll'],
    '/**/js/**':        ['permitAll'],
    '/**/css/**':       ['permitAll'],
    '/**/images/**':    ['permitAll'],
    '/**/favicon.ico':  ['permitAll'],
    '/register/**':     ['permitAll'],
    '/oauth/**':        ['permitAll'],
    '/login/**':        ['permitAll'],
    '/logout/**':       ['permitAll'],
    '/static/**':       ['permitAll'],

    '/dbconsole/**':    ['ROLE_ADMIN'],
    '/greenmail/**':    ['ROLE_ADMIN']
]
