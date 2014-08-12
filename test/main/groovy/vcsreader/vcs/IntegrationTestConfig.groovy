package vcsreader.vcs

class IntegrationTestConfig {
    static final String pathToGit = "/usr/bin/git"
    static final String projectFolder = "/tmp/git-commands-test/git-repo/"
    static final String prePreparedProject = "/tmp/test-repos/git-repo"

    static final def revisions = ["ee618b8f3a5afba3e432e0715bbbdec580a1d32e","504f540c65fb44ce4be3c584990b6344863403d0","0c0be23746e69ee50a60fbc43a9484f56a0b68ac"]

    static final String author = "Some Author"
    static final String firstRevision = revisions[0]
    static final String secondRevision = revisions[1]
    static final String thirdRevision = revisions[2]
}
