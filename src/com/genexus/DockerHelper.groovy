package com.genexus

/**
 * Perform login to a Docker registry.
 *
 * @param args A map containing the following parameters:
 *   - credentialsId: The ID of the Jenkins credential storing the registry credentials (username/password)
 *   - registryEndpoint: The endpoint of the Docker registry
 */
void performDockerLogin(Map args = [:]) {
    try {
        withCredentials([
            usernamePassword(
                credentialsId: args.registryCredentialsId,
                usernameVariable: 'DOCKER_USERNAME',
                passwordVariable: 'DOCKER_PASSWORD')
        ]) {
            sh label: "Login to registry ${args.registryEndpoint}",
                script: "docker login ${args.registryEndpoint} -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}"
        }
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Build a Docker image from a Dockerfile and tag it as the latest.
 *
 * @param args A map containing the following parameters:
 *   - dockerfileRootPath: The root path where the Dockerfile is located
 *   - imageTagName: The tag name for the Docker image
 */
void performDockerBuild(Map args = [:]) {
    try {
        echo "[INFO] WORKSPACE in Docker helper: ${WORKSPACE}"
        echo "Build ./context --> ${args.dockerImageName}:latest"
        sh script: "docker build ./context -t ${args.dockerImageName}:latest"
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Create a Docker image tag.
 *
 * @param args A map containing the following parameters:
 *   - dockerImageName: The name of the existing Docker image to tag
 *   - publishDockerImageName: The name of the new Docker image tag to create
 *   - version: The version tag for the new Docker image tag
 */
void performDockerImageTag(Map args = [:]) {
    try {
        sh label: "Tag ${args.dockerImageName}:latest --> ${args.dockerImageName}:${args.version}",
           script: "docker tag ${args.dockerImageName}:latest ${args.dockerImageName}:${args.version}"
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}
/**
 * Push a Docker image to a Docker registry.
 *
 * @param args A map containing the following parameters:
 *   - publishDockerImageName: The name of the Docker image to push
 *   - version: The version tag of the Docker image to push
 */
void performDockerPushImage(Map args = [:]) {
    try {
        sh script: "docker push ${args.dockerImageName}:${args.version}"
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Remove unused Docker images.
 *
 * This method removes all unused Docker images from the local Docker environment.
 */
void removeUnusedImages() {
    try {
        sh label: "Remove unused Docker images",
           script: "docker rmi -f \$(docker images -q)"
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Pull an image from a private registry.
 *
 * @param args A map containing the following parameters:
 *   - registryEndpoint: The endpoint of the private registry
 *   - credentialsId: The ID of the Jenkins credential storing the registry credentials (username/password)
 *   - imageName: The name of the Docker image to pull
 *   - version: The version tag of the Docker image to pull
 */
void performDockerPullImageFromPrivateRegistry(Map args = [:]) {
    try {
        String registryInsecureUrl = args.registryEndpoint.replace('https://', '')
        docker.withRegistry("${args.registryEndpoint}/", args.credentialsId) {
            sh label: "Download Docker image",
               script: "docker pull ${registryInsecureUrl}/${args.imageName}:${args.version}"
        }
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Remove containers (and related images) declared in docker-compose.yaml project file.
 *
 * @param args A map containing the following parameters:
 *   - composeFileRootPath: The root path where the docker-compose.yaml file is located
 *   - projectName: The name of the Docker Compose project
 */
void performDockerComposeStop(Map args = [:]) {
    try {
        dir(args.composeFileRootPath) {
            sh label: "Stop project ${args.projectName}", 
                script: "docker compose -p ${args.projectName} rm --force --stop"
        }
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Create and start containers using docker-compose.yaml project file.
 *
 * @param args A map containing the following parameters:
 *   - composeFileRootPath: The root path where the docker-compose.yaml file is located
 *   - projectName: The name of the Docker Compose project
 */
void performDockerComposeRun(Map args = [:]) {
    try {
        dir(args.composeFileRootPath) {
            sh label: "Run project ${args.projectName}", 
                script: "docker compose -p ${args.projectName} up"
        }
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Create and start containers using docker-compose.yaml project file in detached mode.
 *
 * @param args A map containing the following parameters:
 *   - composeFileRootPath: The root path where the docker-compose.yaml file is located
 *   - projectName: The name of the Docker Compose project
 */
void performDockerComposeStart(Map args = [:]) {
    try {
        dir(args.composeFileRootPath) {
            sh label: "Start project ${args.projectName} in detached mode", 
                script: "docker compose -p ${args.projectName} up -d"
        }
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

/**
 * Create a Java environment variables file.
 *
 * @param args A map containing the following parameters:
 *   - pipelineDefinition: The pipeline definition map
 *   - deploymentDefinition: The deployment definition map
 *   - dataSource: The type of the data source (e.g., 'SQL Server', 'MySQL')
 *   - javaPackageName: The name of the Java package
 */
void createJavaEnvVarFile(Map args = [:]) {
    String packageName = args.javaPackageName.replace('.', '_').toUpperCase()
    FileHelper file = new FileHelper()
    String extraEnvVariables = ""
    def dbStorage = file.getDatabaseConnection(args.pipelineDefinition, args.deploymentDefinition.databaseId)
    withCredentials([
        usernamePassword(credentialsId: dbStorage.credentialsId, usernameVariable: 'dbStorageUser', passwordVariable: 'dbStoragePwd')
    ]) {
        switch (args.dataSource) {
            case 'SQL Server':
                extraEnvVariables += "GX_${packageName}_DEFAULT_DB_URL=jdbc:sqlserver://${dbStorage.endpoint}:${dbStorage.port};databaseName=${dbStorage.name};encrypt=true;trustServerCertificate=true\n"
                break
            case 'MySQL':
                extraEnvVariables += "GX_${packageName}_DEFAULT_DB_URL=jdbc:mysql://${dbStorage.endpoint}:${dbStorage.port}/${dbStorage.name}?useSSL=false\n"
                break
            default:
                error "Invalid dataSource input: ${args.dataSource}"
                break
        }
        extraEnvVariables += "GX_${packageName}_DEFAULT_USER_ID=${dbStorageUser}\n"
        extraEnvVariables += "GX_${packageName}_DEFAULT_USER_PASSWORD=${dbStoragePwd}\n"
    }
    if (args.deploymentDefinition.gam) {
        def dbGAM = file.getDatabaseConnection(args.pipelineDefinition, args.deploymentDefinition.gam.databaseId)
        withCredentials([
            usernamePassword(credentialsId: dbGAM.credentialsId, usernameVariable: 'dbGAMUser', passwordVariable: 'dbGAMPwd')
        ]) {
            switch (args.dataSource) {
                case 'SQL Server':
                    extraEnvVariables += "GX_${packageName}_GAM_DB_URL=jdbc:sqlserver://${dbGAM.endpoint}:${dbStorage.port};databaseName=${dbGAM.name};encrypt=true;trustServerCertificate=true\n"
                    break
                case 'MySQL':
                    extraEnvVariables += "GX_${packageName}_GAM_DB_URL=jdbc:mysql://${dbGAM.endpoint}:${dbStorage.port}/${dbGAM.name}?useSSL=false\n"
                    break
                default:
                    error "Invalid dataSource input: ${args.dataSource}"
                    break
            }
            extraEnvVariables += "GX_${packageName}_GAM_USER_ID=${dbGAMUser}\n"
            extraEnvVariables += "GX_${packageName}_GAM_USER_PASSWORD=${dbGAMPwd}\n"
        }
        extraEnvVariables += "GX_GAMCONNECTIONKEY=${args.deploymentDefinition.gam.connectionKey}\n"
    }
    if (args.deploymentDefinition.gxflow) {
        def dbGXFlow = file.getDatabaseConnection(args.pipelineDefinition, args.deploymentDefinition.gxflow.databaseId)
        withCredentials([
            usernamePassword(credentialsId: dbGXFlow.credentialsId, usernameVariable: 'dbGXFlowUser', passwordVariable: 'dbGXFlowPwd')
        ]) {
            switch (args.dataSource) {
                case 'SQL Server':
                    extraEnvVariables += "GX_${packageName}_GXFLOW_DB_URL=jdbc:sqlserver://${dbGXFlow.endpoint}:${dbStorage.port};databaseName=${dbGXFlow.name};encrypt=true;trustServerCertificate=true\n"
                    break
                case 'MySQL':
                    extraEnvVariables += "GX_${packageName}_GXFLOW_DB_URL=jdbc:mysql://${dbGXFlow.endpoint}:${dbStorage.port}/${dbGXFlow.name}?useSSL=false\n"
                    break
                default:
                    error "Invalid dataSource input: ${args.dataSource}"
                    break
            }
            extraEnvVariables += "GX_${packageName}_GXFLOW_USER_ID=${dbGXFlowUser}\n"
            extraEnvVariables += "GX_${packageName}_GXFLOW_USER_PASSWORD=${dbGXFlowPwd}\n"
        }
    }
    if (args.deploymentDefinition.logLevel) {
        extraEnvVariables += "GX_LOG_LEVEL=${args.deploymentDefinition.logLevel}\n"
        extraEnvVariables += "GX_LOG_LEVEL_USER=${args.deploymentDefinition.logLevel}\n"
    }
    extraEnvVariables += "GX_LOG_OUTPUT=ConsoleAppender\n"
    args.deploymentDefinition.envs.each { envVariable ->
        extraEnvVariables += "${envVariable.name}=${envVariable.value}\n"
    }
    args.deploymentDefinition.secretsEnvs.each { secretEnvVariable ->
        withCredentials([
            usernamePassword(credentialsId: secretEnvVariable.credentialsId, usernameVariable: 'secretUser', passwordVariable: 'secretPass')
        ]) {
            extraEnvVariables += "${secretEnvVariable.userName}=${secretUser}\n"
            extraEnvVariables += "${secretEnvVariable.passName}=${secretPass}\n"
        }
    }
    writeFile file: 'variables.env', text: extraEnvVariables
}

/**
 * Create a .NET Core environment variables file.
 *
 * @param args A map containing the following parameters:
 *   - pipelineDefinition: The pipeline definition map
 *   - deploymentDefinition: The deployment definition map
 */
void createNetCoreEnvVarFile(Map args = [:]) {
    FileHelper file = new FileHelper()
    String extraEnvVariables = ""
    def dbStorage = file.getDatabaseConnection(args.pipelineDefinition, args.deploymentDefinition.databaseId)
    withCredentials([
        usernamePassword(credentialsId: dbStorage.credentialsId, usernameVariable: 'dbStorageUser', passwordVariable: 'dbStoragePwd')
    ]) {
        extraEnvVariables += "GX_CONNECTION-DEFAULT-DATASOURCE=${dbStorage.endpoint}\n"
        extraEnvVariables += "GX_CONNECTION-DEFAULT-DB=${dbStorage.name}\n"
        extraEnvVariables += "GX_CONNECTION-DEFAULT-USER=${dbStorageUser}\n"
        extraEnvVariables += "GX_CONNECTION-DEFAULT-PASSWORD=${dbStoragePwd}\n"
        extraEnvVariables += "GX_CONNECTION-DEFAULT-SCHEMA=dbo\n"
        extraEnvVariables += "GX_CONNECTION-DEFAULT-OPTS=;Integrated Security=no;\n"
    }
    if (args.deploymentDefinition.gam) {
        def dbGAM = file.getDatabaseConnection(args.pipelineDefinition, args.deploymentDefinition.gam.databaseId)
        withCredentials([
            usernamePassword(credentialsId: dbGAM.credentialsId, usernameVariable: 'dbGAMUser', passwordVariable: 'dbGAMPwd')
        ]) {
            extraEnvVariables += "GX_CONNECTION-GAM-DATASOURCE=${dbGAM.endpoint}\n"
            extraEnvVariables += "GX_CONNECTION-GAM-DB=${dbGAM.name}\n"
            extraEnvVariables += "GX_CONNECTION-GAM-USER=${dbGAMUser}\n"
            extraEnvVariables += "GX_CONNECTION-GAM-PASSWORD=${dbGAMPwd}\n"
            extraEnvVariables += "GX_CONNECTION-GAM-SCHEMA=gam\n"
            extraEnvVariables += "GX_CONNECTION-GAM-OPTS=;Integrated Security=no;\n"
        }
        extraEnvVariables += "GX_GAMCONNECTIONKEY=${args.deploymentDefinition.gam.connectionKey}\n"
    }
    if (args.deploymentDefinition.gxflow) {
        def dbGXFlow = file.getDatabaseConnection(args.pipelineDefinition, args.deploymentDefinition.gxflow.databaseId)
        withCredentials([
            usernamePassword(credentialsId: dbGXFlow.credentialsId, usernameVariable: 'dbGXFlowUser', passwordVariable: 'dbGXFlowPwd')
        ]) {
            extraEnvVariables += "GX_CONNECTION-GXFLOW-DATASOURCE=${dbGXFlow.endpoint}\n"
            extraEnvVariables += "GX_CONNECTION-GXFLOW-DB=${dbGXFlow.name}\n"
            extraEnvVariables += "GX_CONNECTION-GXFLOW-USER=${dbGXFlowUser}\n"
            extraEnvVariables += "GX_CONNECTION-GXFLOW-PASSWORD=${dbGXFlowPwd}\n"
            extraEnvVariables += "GX_CONNECTION-GXFLOW-SCHEMA=gxflow\n"
            extraEnvVariables += "GX_CONNECTION-GXFLOW-OPTS=;Integrated Security=no;\n"
        }
    }
    if (args.deploymentDefinition.logLevel) {
        extraEnvVariables += "GX_LOG_LEVEL=${args.deploymentDefinition.logLevel}\n"
        extraEnvVariables += "GX_LOG_LEVEL_USER=${args.deploymentDefinition.logLevel}\n"
    }
    extraEnvVariables += "GX_LOG_OUTPUT=ConsoleAppender\n"
    extraEnvVariables += "ASPNETCORE_URLS=http://*:80\n"
    args.deploymentDefinition.envs.each { envVariable ->
        extraEnvVariables += "${envVariable.name}=${envVariable.value}\n"
    }
    args.deploymentDefinition.secretsEnvs.each { secretEnvVariable ->
        withCredentials([
            usernamePassword(credentialsId: secretEnvVariable.credentialsId, usernameVariable: 'secretUser', passwordVariable: 'secretPass')
        ]) {
            extraEnvVariables += "${secretEnvVariable.userName}=${secretUser}\n"
            extraEnvVariables += "${secretEnvVariable.passName}=${secretPass}\n"
        }
    }
    writeFile file: 'variables.env', text: extraEnvVariables
}

/**
 * Add logging configuration to a Docker Compose file.
 *
 * @param args A map containing the following parameters:
 *   - composeFilePath: The path to the docker-compose.yaml file
 *   - loggingServer: The logging server address
 *   - loggingTag: The tag for logging
 */
void addDockerComposeLoggingTags(Map args = [:]) {
    try {
        boolean fileIsValid = fileExists(args.composeFilePath)
        if (!fileIsValid) {
            error "composeFilePath not found in ${args.composeFilePath}"
        }
        LinkedHashMap compose = readYaml(file: args.composeFilePath)
        compose.services.gxapp.put('logging', new LinkedHashMap())
        compose.services.gxapp.logging.put('driver', 'gelf')
        compose.services.gxapp.logging.put('options', new LinkedHashMap())
        compose.services.gxapp.logging.options.put('gelf-address', args.loggingServer)
        compose.services.gxapp.logging.options.put('tag', args.loggingTag)
        
        writeYaml(file: args.composeFilePath, data: compose, overwrite: true)
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    }
}

return this
