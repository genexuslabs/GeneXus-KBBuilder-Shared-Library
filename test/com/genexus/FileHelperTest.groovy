package com.genexus

import com.genexus.FileHelper

class FileHelperTest {

    static void main(String[] args) {
        def instance = new FileHelper() // Create an instance of your class

        // Define test cases
        def tests = [
            ['1.3.5', '88', '', 0, '1.3.88'],
            ['2.1.5', '457', 'beta', 0, '2.1.0-beta.457'],
            ['1.3.5', '88', '', 100, '101.3.88']
        ]

        // Run tests
        tests.each { testCase ->
            String version = instance.standarizeVersionForSemVer(testCase[0], testCase[1], testCase[2], testCase[3])
            assert version == testCase[4] : "Expected ${testCase[4]}, but got ${version}"
        }

        println "All tests passed!"
    }
}
