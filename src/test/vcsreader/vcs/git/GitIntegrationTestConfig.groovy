package vcsreader.vcs.git

import groovy.json.JsonSlurper

class GitIntegrationTestConfig {
    static config = new JsonSlurper().parse(new File("src/test/vcsreader/vcs/git/git-test-config.json"))

    static final String pathToGit = config["pathToGit"] as String
    static final String referenceProject = config["referenceProject"] as String
    static final String nonExistentPath = "/tmp/non-existent-path" as String

    static final List<String> revisions = config["revisions"] as List

    static final String author = config["author"] as String
    static final String firstRevision = revisions[0]
    static final String secondRevision = revisions[1]
    static final String thirdRevision = revisions[2]
}
