package vcsreader.vcs.svn
import groovy.json.JsonSlurper

class SvnIntegrationTestConfig {
    static config = new JsonSlurper().parse(new File("src/test/vcsreader/vcs/svn/svn-test-config.json"))

    static final String pathToSvn = config["pathToSvn"] as String
    static final String repositoryUrl = config["svnRepositoryUrl"] as String
    static final String nonExistentUrl = "file://non-existent/url"

    static final String author = config["author"] as String
    static final String firstRevision = "1"
    static final String secondRevision = "2"
}
