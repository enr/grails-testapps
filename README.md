Grails test apps plugin
=======================

[![Build Status](https://travis-ci.org/enr/grails-testapps.png?branch=master)](https://travis-ci.org/enr/grails-testapps)

Provides a script and a DSL to create Grails applications to test your plugins.

Useful to create test applications for plugins you are developing or simply to create Grails apps programmatically.

**This plugin is heavily inspired from the script described from Burt Beckwith in the book Programming Grails and used in the spring-security-* plugins.**


Usage
-----

Compile the project with `grails compile` and run the script:

    grails create-testapps /path/to/testapps.config.groovy

testapps.config.groovy is the file containing the description of the apps you want to create.

The script assumes there is a directory `testapps/` in the root of the plugin under test, containing template files to be copied in the final app.


How it works
------------

For every app in the described in the configuration file, the script:

- creates the test application using `grails create-app`

- adds the path `./testapps-config.groovy` to config locations (in `grails-app/conf/Config.groovy`);  
  this way you can put a file with the same name in the templates (ie `testapps/testapps-config.groovy`) dir and this will be loaded in the app

- adds a lot of Maven repo to BuildConfig to minimize deps fetching problems

- install plugins and dependencies registered in the configuration for the given app

- run scripts registered in the configuration for the given app

- copy `testapps/` directory contents to the apps root directory:
  `all/` directory contents are copied into every app defined in the testapps file, other directories are copied in the app named after the directory overriding the `all/` files.


Create apps with different Grails versions
------------------------------------------

Usually if you have various Grails installation in a box, they share a common base path (in my box is `/opt/grails`).

To create apps for every Grails installation, set a common root dir outside the app-specific configuration block, and
set the Grails home to something similar to `grailsHomeRoot + '/grails-' + grailsVersion`

Example:

```groovy

    // base dir for different Grails installations
    String grailsHomeRoot = '/opt/grails'
    v220 {
        // the Grails version to use for app v220
        grailsVersion = '2.2.0'
        grailsHome = "${grailsHomeRoot}/grails-${grailsVersion}"
        //[...] rest of configuration
    }
    v210 {
        grailsVersion = '2.1.0'
        grailsHome = "${grailsHomeRoot}/grails-${grailsVersion}"
        //[...] rest of configuration
    }

```

5 minutes test
--------------

[testapps directory](testapps) contains a working and commented example: run it with: `grails compile && grails create-testapps testapps/sample-testapps.config.groovy -plain-output`


See also
--------

- one of the scripts inspiring this plugin: [spring-security-ui/CreateS2UiTestApps](https://github.com/grails-plugins/grails-spring-security-ui/blob/master/scripts/CreateS2UiTestApps.groovy)

- another script in [grails-platform-ui plugin](https://github.com/MerryCoders/grails-platform-ui/blob/master/scripts/CreatePlatformUiTestApps.groovy)


License
-------

Apache 2.0 - see LICENSE file.

   Copyright 2014 grails-testapps contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
