### How to build?
Use [gradle](http://gradle.org/) tasks, e.g. `gradle jar`.
For offline builds uncomment `maven { url 'lib' }` in `build.gradle`
which will make gradle use dependencies from ``lib`` folder.

There are several VCS integration tests which create temporary repositories with predefined commit history.
To run these tests `git`, `hg` and `svn` commands have to be installed.
You can configure path to the command using system properties:
`vcsreader.test.gitPath`, `vcsreader.test.hgPath`, `vcsreader.test.svnPath`, `vcsreader.test.svnAdminPath`.


### Things to do
 - support for listing and requesting commits from particular branch
 - optional support to log merge commits (currently merge commits are not logged, only original commit from another branch is logged)
