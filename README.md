### VCS Reader
This is minimal Java API to read commits from version control systems.
It uses native VCS commands to clone/update/log commits and currently supports 
[Git](https://git-scm.com/), 
[Mercurial](https://www.mercurial-scm.org/) and 
[Subversion](https://subversion.apache.org/).


### Why?
I didn't find any similar libraries to read commit history.


### API Example
```groovy
package vcsreader

import vcsreader.vcs.git.GitSettings
import vcsreader.vcs.git.GitVcsRoot
import static vcsreader.Change.Type.MODIFICATION
import static vcsreader.lang.DateTimeUtil.date
import static vcsreader.lang.DateTimeUtil.dateTime


def localPath = "/tmp/junit"
def repoUrl = "https://github.com/junit-team/junit"
def gitSettings = GitSettings.defaults().withGitPath("/usr/bin/git")
def vcsRoot = new GitVcsRoot(localPath, repoUrl, gitSettings)
def project = new VcsProject(vcsRoot)

def cloneResult = project.cloneToLocal()
assert cloneResult.isSuccessful()

def logResult = project.log(date("01/01/2013"), date("01/01/2014"))
assert logResult.commits().size() == 242

def commit = logResult.commits().last()
assert commit.revision == "0e1a559e1371aa9929ca4f61f87cf8f9a5923ce7"
assert commit.revisionBefore == "4e9f1a65ca8d794db54260b4f2e5b078d949fdda"
assert commit.commitTime == dateTime("Fri Dec 27 18:54:49 GMT 2013")
assert commit.author == "Stefan Birkner"
assert commit.comment == "Override toString() with meaningful implementation.\n\nThe new implementation provides more useful information."
assert commit.changes.first().type == MODIFICATION
assert commit.changes.first().filePath == "src/main/java/org/junit/runners/model/FrameworkField.java"
```


### How to build?
Use [gradle](http://gradle.org/). 
For example, ```gradle jar``` in project folder.
All dependencies are available in ```lib``` folder.

There are several VCS integration tests which require temporary repositories with predefined commit history.
Use ruby scripts to generate them:
 - src/test/vcsreader/vcs/git/git-create-repo.rb
 - src/test/vcsreader/vcs/hg/hg-create-repo.rb
 - src/test/vcsreader/vcs/svn/svn-create-repo.rb
 

### Things to do
 - support for branches
 - ability to cancel VCS commands by killing underlying process (e.g. if log takes very long time)
 - rewrite ruby scripts for generating test repositories in jvm language so that it's easier to use them in CI 
 - optional support to log merge commits (currently merge commits are not logged, only original commit from another branch is logged)
 