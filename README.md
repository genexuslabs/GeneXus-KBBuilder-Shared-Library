# GeneXus-KBBuilder-Shared-Library

Welcome to the **GeneXus-KBBuilder-Shared-Library** repository! This repository contains a collection of scripts designed to facilitate the integration of GeneXus tasks with Jenkins, enabling seamless automation and continuous integration for your GeneXus projects.

## Table of Contents

- [Features](#features)
- [Folder Structure](#folder-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

- Simplify the configuration of Jenkins jobs for GeneXus projects.
- Support for various GeneXus versions and configurations.
- Easy-to-use scripts that can be customized for specific project needs.

## Folder Structure

The repository is organized into the following folders based on the [Jenkins Shared Library documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#directory-structure):

```text
(root)
+- src                               # Groovy source files for reusable and more complex classes.
|   +- com
|       +- genexus                   # Modules providing helper methods for GeneXus related operations.
|       |   +- CloudHelper.groovy    #   Provides utility methods for aws interactions.
|       |   +- DockerHelper.groovy   #   Encapsulates Docker image and container operations.
|       |   +- FileHelper.groovy     #   Offers common file and directory manipulation utilities.
|       |   +- UnixHelper.groovy     #   Offers common utilities for Unix environment.
|       |   +- FlywayHelper.groovy     #   Offers common utilities for managing flyway.
|       |   +- GeneXusHelper.groovy  #   Core utilities for interacting with GeneXus installations or specifications.
|       |   +- GitHelper.groovy      #   Simplifies Git version control operations within pipelines.
|       |   +- GXDeployEngineHelper.groovy # Utilities for managing GeneXus Application Deployment engine tasks.
|       |   +- GXModuleHelper.groovy #   Helps with managing GeneXus modules.
|       |   +- GXTargetHelper.groovy #   Assists with GeneXus target environment configurations and properties.
|       |   +- NotificationHelper.groovy # Helps with managing sending various types of notifications.
|       |   +- PropertiesHelper.groovy #  Handles loading, parsing, and managing GeneXus kb properties.
|       +- kbbuilder                 # Modules providing helper methods for KBBuilder specific operations.
|       |   +- GXFlowHelper.groovy   #   Utilities for interacting with GeneXus Flow (workflow/BPM).
|       |   +- GXGamHelper.groovy    #   Assists with GeneXus Access Manager (GAM) related operations.
+- vars                              # Functions for global variables and custom Pipeline steps.
|   +- applyPattern.groovy          #   Applies a predefined pattern such as "Work With" to a KB.
|   +- applyReorganization.groovy   #   Executes a database reorganization process.
|   +- buildConfigurationEnvironment.groovy # Builds an environment without impacting the database.
|   +- buildCustomEnvironment.groovy  #   Builds a custom GeneXus environment.
|   +- buildInstallationEnvironment.groovy # Builds an environment impacting the database.
|   +- checkPendingReorg.groovy     #   Checks if there are pending database reorganizations.
|   +- closeKnowledgeBase.groovy    #   Closes the currently open GeneXus Knowledge Base.
|   +- configureDataStore.groovy    #   Configures a specific data store connection in an environment.
|   +- configureGAMDatastore.groovy #   Configures the GeneXus Access Manager (GAM) data store.
|   +- configureGXFlowDatastore.groovy # Configures the GXFlow (workflow) data store.
|   +- createEnvironment.groovy     #   Creates a new GeneXus environment (e.g., Java, .NET).
|   +- exportReorganization.groovy  #   Exports database reorganization scripts or impact analysis.
|   +- exportXPZ.groovy             #   Exports GeneXus full Knowledge Base as an XPZ file.
|   +- importXPZ.groovy             #   Imports a Knowledge Base from an XPZ file.
|   +- installGeneXusUsingURI.groovy # Installs GeneXus from a specified URI.
|   +- markDBReorganized.groovy     #   Marks the database as having been reorganized.
|   +- packageAngularDU.groovy      #   Packages an Angular-based Deployment Unit from GeneXus.
|   +- packageLocalDU.groovy        #   Packages a local Deployment Unit.
|   +- packageDockerDU.groovy        #   Packages a local Deployment Unit and creates docker context.
|   +- packageModule.groovy         #   Packages a GeneXus module for distribution or deployment.
|   +- publishModule.groovy         #   Publishes a GeneXus module to a repository or server.
|   +- restoreInstalledModule.groovy # Restores an installed GeneXus module from a backup or source.
|   +- runUnitTestSuite.groovy      #   Executes a defined suite of unit tests within the Knowledge Base.
|   +- scpSync.groovy               #   Synchronizes files or directories to a remote server using SCP.
|   +- sendEmailNotification.groovy #   Sends a customizable email notification.
|   +- sendEmailNotificationGxServer.groovy # Sends an email notification listing only gxserver commits.
|   +- updateInstalledModule.groovy #   Updates an already installed GeneXus module to a new version.
+- resources                         # External resource files.
|   +- com
|       +- genexus
|           +- bashScripts           # Contains Bash Scripts.
|           |   +- login_to_ecr.sh
|           +- notificationTemplates # Templates for mail notifications used in GeneXus pipeline contexts.
|           |   +- emailBuildResult.html.groovy
|           |   +- emailBuildResult2.html.groovy
|           +- pwshScripts
|           |   +- common            #   Common utility PowerShell scripts.
|           |   |   +- update-from-nuget.ps1 #   Updates components/dependencies from a NuGet feed.
|           |   |   +- update-from-s3.ps1    #   Updates components/dependencies from an AWS S3 bucket.
|           |   |   +- update-from-zip.ps1   #   Updates components/dependencies from a local or remote ZIP file.
|           |   |   +- upload-to-s3.ps1      #   Uploads files or artifacts to an AWS S3 bucket.
|           |   |   +- zip-directory.ps1     #   Compresses a specified directory into a ZIP archive.
|           |   +- gxInstallation    #   PowerShell scripts specific to GeneXus installation and environment management.
|           |   |   +- addGeneXusInstallationFlag.ps1 # Sets a flag or marker related to a GeneXus installation.
|           |   |   +- configAppSettingsValue.ps1    # Configures 'appSettings' values in .NET configuration files.
|           |   |   +- configureProtectionServer.ps1 # Configures GeneXus Protection Server settings.
|           |   |   +- deleteGeneXusInstallation.ps1 # Removes or uninstalls a GeneXus installation.
|           |   |   +- updateGeneXusInstallationByURI.ps1 # Updates a GeneXus installation using a provided URI source.
|           +- templates             # MsBuild templates used for GeneXus BL calls.
|           |   +- cdxci.msbuild
|           |   +- properties.msbuild
|           +- utils                 # Utility pipelines or resources for various helper tasks.
|           |   +- reachDesiredJobBuildNumber
|           |   |   +- Jenkinsfile   # Pipeline used for making a jenkins job reach a desired build number.
|           |   +- saveCredentials   
|           |   |   +- Jenkinsfile   # Pipeline used for saving a credential from jenkins to a file in the workspace.
```

## Contributing

Contributions to the GeneXus-KBBuilder-Shared-Library are not currently being accepted. However, you are welcome to use this repository as inspiration for your own projects or to adapt the scripts for your specific needs.

Thank you for your understanding!

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for more details.

Thank you for your interest in the GeneXus-KBBuilder-Shared-Library! We hope you find it useful for your GeneXus and Jenkins integration needs.
