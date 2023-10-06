package com.genexus

/**
 * This methods 
 * @param filePath
 */
String concatArgs(String msbuildGenArgs, String propName, String propValue) {
    msbuildGenArgs = msbuildGenArgs + " /p:" + propName + "=" + "\"" + propValue + "\""
    return msbuildGenArgs
}

return this
