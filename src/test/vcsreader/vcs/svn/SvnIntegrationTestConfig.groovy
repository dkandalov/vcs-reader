package vcsreader.vcs.svn
import groovy.json.JsonSlurper

class SvnIntegrationTestConfig {
    static String pathToSvn
    static String repositoryUrl
    static String nonExistentUrl
    static String author

    static initTestConfig() {
        def configFile = new File("src/test/vcsreader/vcs/svn/svn-test-config.json")
        if (!configFile.exists()) {
            throw new IllegalStateException("Cannot find " + configFile.name + ".\n" +
                    "Please make sure you run tests with project root as working directory.")
        }
        def config = new JsonSlurper().parse(configFile)
        pathToSvn = config["pathToSvn"] as String
        repositoryUrl = config["svnRepositoryUrl"] as String
        nonExistentUrl = "file://non-existent/url"
        author = config["author"] as String

        if (!new File(pathToSvn).exists())
            throw new FileNotFoundException("Cannot find '" + pathToSvn + "'. Please check content of '" + configFile.absolutePath + "'")
        if (!new File(repositoryUrl.replace("file://", "")).exists())
            throw new FileNotFoundException("Cannot find '" + repositoryUrl + "'. Please run svn-create-repo.rb")
    }

    static String revision(int n) {
        String.valueOf(n)
    }
}
