### VCS Reader
This is minimal Java API to read commits from version control systems.

It uses native VCS commands to clone/update/log commits and currently supports 
[Git](https://git-scm.com/), 
[Mercurial](https://www.mercurial-scm.org/) and 
[Subversion](https://subversion.apache.org/).


### Why?
Analysing code history is fun but there seems to be no simple java libraries to do this.


### Example
```groovy
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
assert commit.authorName == "Stefan Birkner"
assert commit.comment == "Override toString() with meaningful implementation.\n\nThe new implementation provides more useful information."
assert commit.changes.first().type == MODIFICATION
assert commit.changes.first().filePath == "src/main/java/org/junit/runners/model/FrameworkField.java"

```