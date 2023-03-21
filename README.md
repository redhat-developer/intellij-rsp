# Server Connector
[plugin-repo]: https://plugins.jetbrains.com/plugin/16072-runtime-server-protocol-connector-by-red-hat
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/16072-runtime-server-protocol-connector-by-red-hat.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/16072-runtime-server-protocol-connector-by-red-hat.svg

![Java CI with Gradle](https://github.com/redhat-developer/intellij-rsp/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

[![Gitter](https://badges.gitter.im/redhat-developer/server-connector.svg)](https://gitter.im/redhat-developer/server-connector)

## Overview

A JetBrains IntelliJ plugin for interacting with Application Servers of all types. This extension is currently in Preview Mode and works with JBoss, Wildfly, JBoss EAP, Tomcat, Glassfish, Felix, Karaf, Jetty, Minishift, CDK, and CRC. 

## Build / Package
Type `./gradlew buildPlugin`

## Release notes

See the [change log][plugin-repo].

Contributing
============
This is an open source project open to anyone. This project welcomes contributions and suggestions!

For information on getting started, refer to the [CONTRIBUTING instructions](CONTRIBUTING.md).


Feedback & Questions
====================
If you discover an issue please file a bug and we will fix it as soon as possible.
* File a bug in [GitHub Issues](https://github.com/redhat-developer/intellij-rsp/issues).
* Chat with us on [Gitter](https://gitter.im/redhat-developer/server-connector).

UI Testing
==========
You can perform UI testing by running the following command:
```sh
./gradlew integrationTest
```

License
=======
EPL 2.0, See [LICENSE](LICENSE) for more information.
