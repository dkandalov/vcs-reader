package vcsreader.vcs

class GitIntegrationTestConfig {
    static final String pathToGit = "/usr/bin/git"
    static final String prePreparedProject = "/tmp/test-repos/git-repo"

    static final def revisions = ["ee618b8f3a5afba3e432e0715bbbdec580a1d32e","504f540c65fb44ce4be3c584990b6344863403d0","0c0be23746e69ee50a60fbc43a9484f56a0b68ac","6c35a07ef3df6a21ce63aef01d4a15afa048fae4","4f5ec64c7c516622fc6badeb8b592091c3866d4f","eac84fad123baa8ba33857ad686180f3286ecbed"]

    static final String author = "Some Author"
    static final String firstRevision = revisions[0]
    static final String secondRevision = revisions[1]
    static final String thirdRevision = revisions[2]
}
