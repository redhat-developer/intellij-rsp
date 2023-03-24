# Server Connector
[plugin-repo]: https://plugins.jetbrains.com/plugin/16072-runtime-server-protocol-connector-by-red-hat
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/16072-runtime-server-protocol-connector-by-red-hat.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/16072-runtime-server-protocol-connector-by-red-hat.svg

![Java CI with Gradle](https://github.com/redhat-developer/intellij-rsp/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

[![Gitter](https://badges.gitter.im/redhat-developer/server-connector.svg)](https://gitter.im/redhat-developer/server-connector)

## Overview

An IntelliJ plugin for interacting with Application Servers of all types. This extension is currently in Preview Mode and works with JBoss, Wildfly, JBoss EAP, Tomcat, Glassfish, Felix, Karaf, Jetty, Minishift, CDK, and CRC. 

## Build / Package
Type `./gradlew buildPlugin`

## What's Included

This extension incldues support for two different RSP servers, or, servers that implement the "Runtime Server Protocol" API. The two RSPs are the "Red Hat Server Connector" and the "Community Server Connector". These two RSP servers are not bundled, but can be downloaded and updated using this extension. 


## Actions and Features

This extension supports a number of actions for interacting with supported RSPs and server adapters; these are accessible via the context menu in the main view.

### Available Commands on the RSP
   * `Download / Update RSP` - Download a new instance or update an existing RSP. 
   * `Start RSP` - Start the selected RSP.
   * `Stop RSP` - Stop the selected RSP.
   * `Create Server` - Create a server that the selected RSP supports from a file or folder on disk.
   * `Download Server` - Create a server that the selected RSP supports by downloading it, and extracting it first. 

### Available Commands on a Server
   * `Delete Server` - Delete the selected server adapter (but not its underlying files).
   * `Start Server (run)` - Start the given server in run mode
   * `Start Server (debug)` - Start the given server in debug mode and, if possible, connect a remote debugger.
   * `Restart Server (run)` - Restart the selected server in run mode
   * `Restart Server (debug)` - Restart the given server in debug mode and, if possible, connect a remote debugger.
   * `Stop Server` - Request the server stop. 
   * `Terminate Server` - Request the server be terminated if possible.
   * `Add Deployment` - Add a deployable file or folder to the server to be published.
   * `Remove Deployment from Server` - Remove a selected deployment from the server.
   * `Publish Server (Full)` - Publish the server, synchronizing the content of deployments from your workspace to the server.
   * `Publish Server (Incremental)` - Publish the server with recent changes, synchronizing the content of deployments from your workspace to the server.
   * `Edit Server` - View a JSON representation of your server in an editor, and submit any changed properties back to the RSP. 
   * `Server Actions` - Some server types may expose to the user arbitrary actions that the user may invoke, such as changing some configuration options, opening a web browser, or editing a configuration file. These server-contributed actions have few restrictions placed on them by the framework other than what may be done on the client-side. 

## Server Parameters
   To change Server Parameters, right-click on the server you want to edit and select `Edit Server`

### Global Server Parameters
   These settings are valid for all servers

   * `"id"` - id server (read-only field, it cannot be changed)
   * `"args.override.boolean"` - allow to override program and vm arguments if set to true. The first time this flag is set to true and the server is started, two other parameters will be generated "args.vm.override.string" and "args.program.override.string". 
   * `"server.home.dir"` - the path where the server runtime is stored (read-only field, it cannot be changed)
   * `"deployables"` - the list of deployables. It contains all informations related to each deployable.
   * `"server.autopublish.enabled"` - Enable the autopublisher
   * `"server.autopublish.inactivity.limit"` - Set the inactivity limit before the autopublisher runs
   * `"vm.install.path"` - A string representation pointing to a java home. If not set, rsp-ui.rsp.java.home will be used instead

### Provisional Global Server Parameters
   These settings may eventually be supported by all servers, but these settings are Provisional and may be changed before becoming official API. 

   * `"args.vm.override.string"` - allow to override vm arguments. Once you edited this flag, *make sure "args.override.boolean" is set to true before launching your server. Otherwise the server will attempt to auto-generate the launch arguments as it normally does.*
   * `"args.program.override.string"` - allow to override program arguments. Once you edited this flag, *make sure "args.override.boolean" is set to true before launching your server. Otherwise the server will attempt to auto-generate the launch arguments as it normally does.*
   * `"mapProperty.launch.env"` - allow to override or add to the environment being passed to a server upon startup. This property's value should be a object with a set of key-value pairs, where the key should be a desired environment variable, and the value being the value of that object.*
   
### Provisional Project Structure Details
   The following project structure options may not be supported by all server types and deployment types. These details are Provisional and may be changed before becoming official API. 
   
   A workspace project may choose to have a `.rsp/rsp.assembly.json` file which may dictate very simple packaging instructions. Many server types will attempt to use this packaging file for both incremental and full publish events, so that the user experience can be improved without requiring full builds with a user's chosen build system for each change. 
   
   Attempts will also be made to interpret a project's `.settings/org.eclipse.wst.common.component` file, though current integration issues with jdt.ls make this not very useful at the moment. 
   
#### .rsp/rsp.assembly.json file structure
An example packaging file may look like this:

```
{
	"mappings": [
		{
			"source-path": "target/classes/",
			"deploy-path": "/WEB-INF/classes/"
		},
		{
			"source-path": "target/rob-hello/",
			"deploy-path": "/"
		},
		{
			"source-path": "src/main/resources/",
			"deploy-path": "/"
		},
		{
			"source-path": "src/main/webapp/",
			"deploy-path": "/"
		},
	]
}
```

A single top-level element name mappings has a value of an array of individual mappings. Each mapping has a source-path and a deploy-path. It is assuemd that the same file may be in multiple folders. A xml file, for example, may be available as a source file, but also exist in a build output directory. A .class file, on the other hand, may exist in a java incremental builder output folder, as well as in a build system output folder. 

Mappings should be arranged such that the most up-to-date folder is near the bottom of the list, so that if the server iterates through them in order, the most recent change will be the last one copied in. 


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
