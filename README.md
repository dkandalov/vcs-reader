### VCS Reader
This is a Java library with minimal API to read commits from version control systems (VCS).
It uses native VCS commands to clone/update/log commits and currently supports 
[Git](https://git-scm.com/), 
[Mercurial](https://www.mercurial-scm.org/) and 
[Subversion](https://subversion.apache.org/).


### Why?
There are libraries for accessing VCS but they only support one VCS.
They have additional features for making changes in VCS but this makes API more complicated.
VcsReader was created to support read-only access to multiple VCS with single API.


### API Example
(Using Java API from Groovy)
```groovy
// setup project
def gitSettings = GitSettings.defaults()
def repositoryUrl = "https://github.com/junit-team/junit"
def localPath = "/tmp/junit"
def vcsRoot = new GitVcsRoot(localPath, repositoryUrl, gitSettings)
def vcsProject = new VcsProject(vcsRoot)

// clone from GitHub (cloning is optional, already cloned project can be used)
def cloneResult = vcsProject.cloneToLocal()
assert cloneResult.isSuccessful()

// log commits within date range
def logResult = vcsProject.log(date("01/01/2013"), date("01/01/2014"))
assert logResult.commits().size() == 242

// read commits data
def commit = logResult.commits().last()
assert commit.revision == "0e1a559e1371aa9929ca4f61f87cf8f9a5923ce7"
assert commit.revisionBefore == "4e9f1a65ca8d794db54260b4f2e5b078d949fdda"
assert commit.time == dateTime("Fri Dec 27 18:54:49 GMT 2013")
assert commit.author == "Stefan Birkner"
assert commit.message == "Override toString() with meaningful implementation.\n\nThe new implementation provides more useful information."

def change = commit.changes.first()
assert change.type == MODIFIED
assert change.filePath == "src/main/java/org/junit/runners/model/FrameworkField.java"
assert change.fileContent().value.contains("public class FrameworkField")
```


### Features
 - log commits and changed file content within date range
 - clone and update repository (for git and hg)
 - automatic detection of file content charset and its conversion to Java string
 - svn commits are filtered to only include changes under source root
 - merge commits are ignored, original author and time are reported
 - even though API is the same for all VCS, svn (unlike git/hg) has dummy implementation for `clone` and `update`.
   This is because there is no cloning in svn and logging svn history can be done by directly connecting to server.
 - to access private repos VCS command line has to be setup to automatically authenticate with the server (e.g. using its config or SSH keys)


### Installation
Maven:
```
<dependency>
    <groupId>org.vcsreader</groupId>
    <artifactId>vcsreader</artifactId>
    <version>1.0.0</version>
</dependency>
```
Gradle:
```
repositories {
    mavenCentral()
}
dependencies {
	compile 'org.vcsreader:vcsreader:1.0.0'
}

```


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
 - ability to cancel VCS commands by killing underlying process (e.g. if log takes very long time)
 - optional support to log merge commits (currently merge commits are not logged, only original commit from another branch is logged)
