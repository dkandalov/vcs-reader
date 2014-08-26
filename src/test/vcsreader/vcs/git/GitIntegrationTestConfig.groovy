package vcsreader.vcs.git

import groovy.json.JsonSlurper

class GitIntegrationTestConfig {
    static config = new JsonSlurper().parse(new File("src/test/vcsreader/git-test-config.json"))

    static final String pathToGit = config["pathToGit"]
    static final String referenceProject = config["referenceProject"]
    static final String nonExistentPath = "/tmp/non-existent-path"

    static final def revisions = config["revisions"]

    static final String author = config["author"]
    static final String firstRevision = revisions[0]
    static final String secondRevision = revisions[1]
    static final String thirdRevision = revisions[2]
}
