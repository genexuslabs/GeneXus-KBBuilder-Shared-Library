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
+- src                       # Groovy source files for reusable and more complex classes.
|   +- com
|       +- genexus           # Modules providing helper methods for GeneXus related operations, such as setting kb properties or creating a docker compose file for GeneXus apps.
|       +- kbbuilder         # Modules providing helper methods for KBBuilder specific operations.
+- vars                      # Global functions that are used to build and configure GeneXus applications in a Jenkins pipeline environment.
+- resources                 # Contains files such as scripts, templates, and configuration data.
|   +- com
|       +- genexus           # Houses various scripts, templates, and utilities for GeneXus tasks and pipelines.  
```

## Contributing

Contributions to the GeneXus-KBBuilder-Shared-Library are not currently being accepted. However, you are welcome to use this repository as inspiration for your own projects or to adapt the scripts for your specific needs.

Thank you for your understanding!

## Versions
- v2.0.4
  - Remover hardcoded propery '/use custom JDBC URL'
 
- v2.0.3
  - Add flag: cleanCustomSpecialFolders in intallGeneXusUsingURI to prevent unchecked accumulation of generated genexus installation directories

- v2.0.2
  - Fix sync flyway iac reorg cript extention
   
- v2.0.1
  - Add git pull method, remove pull before add and commit

- v2.0.0
  - Integrate genexus docker deploy DU instead genexus local package + create docker context

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for more details.

Thank you for your interest in the GeneXus-KBBuilder-Shared-Library! We hope you find it useful for your GeneXus and Jenkins integration needs.
