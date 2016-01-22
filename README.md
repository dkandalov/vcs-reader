### VCS Reader
This is a Java library with minimal API to read commits from version control systems (VCS).
It uses native VCS commands to clone/update/log commits and currently supports 
[Git](https://git-scm.com/), 
[Mercurial](https://www.mercurial-scm.org/) and 
[Subversion](https://subversion.apache.org/).


### Why?
No libraries with API to read commit history from different VCS.


### API Example
(Using Java API from Groovy)
```groovy
// setup project
def gitSettings = GitSettings.defaults()
def vcsRoot = new GitVcsRoot("/tmp/junit", "https://github.com/junit-team/junit", gitSettings)
def vcsProject = new VcsProject(vcsRoot)

// clone from GitHub (cloning is optional, already clone project can be used)
def cloneResult = vcsProject.cloneToLocal()
assert cloneResult.isSuccessful()

// log commits within date range
def logResult = vcsProject.log(date("01/01/2013"), date("01/01/2014"))
assert logResult.commits().size() == 242

// read commits data
logResult.commits().last().with {
	assert revision == "0e1a559e1371aa9929ca4f61f87cf8f9a5923ce7"
	assert revisionBefore == "4e9f1a65ca8d794db54260b4f2e5b078d949fdda"
	assert time == dateTime("Fri Dec 27 18:54:49 GMT 2013")
	assert author == "Stefan Birkner"
	assert message == "Override toString() with meaningful implementation.\n\nThe new implementation provides more useful information."

	changes.first().with {
		assert type == MODIFIED
		assert filePath == "src/main/java/org/junit/runners/model/FrameworkField.java"
		assert fileContent().value.contains("public class FrameworkField")
	}
}
```


### How to build?
Use [gradle](http://gradle.org/) tasks, e.g. ``gradle jar``.
For offline builds all dependencies are committed in ``lib`` folder.

There are several VCS integration tests which require temporary repositories with predefined commit history.
Use ruby scripts to generate them:
 - ``src/test/vcsreader/vcs/git/git-create-repo.rb``
 - ``src/test/vcsreader/vcs/hg/hg-create-repo.rb``
 - ``src/test/vcsreader/vcs/svn/svn-create-repo.rb``
 

### Things to do
 - support for branches
 - ability to cancel VCS commands by killing underlying process (e.g. if log takes very long time)
 - rewrite ruby scripts for generating test repositories in a JVM language so that it's easier to use them in CI
 - optional support to log merge commits (currently merge commits are not logged, only original commit from another branch is logged)
 
