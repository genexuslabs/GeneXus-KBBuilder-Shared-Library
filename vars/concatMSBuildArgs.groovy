/*
 * Job concatMSBuildArgs >> This method takes three arguments and combines them to form a text string representing MSBuild command-line arguments, but with a specific property (propName) set to a specific value (propValue).
 *
 * @Params
 * +- msbuildGenArgs: This is a text string representing command-line arguments for a build tool called MSBuild (typically used in the Microsoft development environment). These arguments can include options, file paths, etc.
 * +- propName: This is a property name that will be used in the context of MSBuild.
 * +- propValue: This is the value of the property corresponding to propName.
 */

def call(String msbuildGenArgs, String propName, String propValue) {
    msbuildGenArgs = msbuildGenArgs + " /p:" + propName + "=" + "\"" + propValue + "\""
    return msbuildGenArgs
}