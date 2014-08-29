package vcsreader.vcs.svn
import groovy.json.JsonSlurper

class SvnIntegrationTestConfig {
    static config = new JsonSlurper().parse(new File("src/test/vcsreader/vcs/svn/svn-test-config.json"))

    static final String pathToSvn = config["pathToSvn"] as String
    static final String repositoryUrl = config["svnRepositoryUrl"] as String
}
