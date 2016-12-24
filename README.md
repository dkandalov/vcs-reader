## Vcs Reader
This is a library for Java (and JVM languages) with minimal API to read information about commits from version control systems (VCS).
It uses native commands to clone/update/log commits and currently supports 
[Git](https://git-scm.com/), 
[Mercurial](https://www.mercurial-scm.org/), 
[Subversion](https://subversion.apache.org/).


### Why?
Libraries which can read commits from VCS usually support only one VCS
and have lots of additional features which make them complicated.
Vcs Reader was designed to support read-only access to multiple VCS with single API.


### API Example
```java
public class ReadCommits {
	public static void main(String[] args) {
		GitVcsRoot gitVcsRoot = new GitVcsRoot(".hamcrest-clone", "https://github.com/hamcrest/JavaHamcrest");
		VcsProject vcsProject = new VcsProject(gitVcsRoot);

		if (!new File(gitVcsRoot.repoFolder()).exists()) {
			System.out.println("Cloning...");
			vcsProject.cloneIt();
		}

		LogResult logResult = vcsProject.log(TimeRange.beforeNow());

		for (VcsCommit commit : logResult.commits()) {
			System.out.println(commit.getDateTime() + " " + commit.getAuthor());
		}
	}
}
```
You can find more examples [here](https://github.com/dkandalov/vcs-reader-examples).


### Features
 - Log commits and changed files (including their content) within date range.
 - Automatic detection of file content charset and its conversion to Java string.
 - Clone and update repository (for git and hg).
 - Access to private repos which require authentication by VCS command line 
   which has to be setup to automatically authenticate (e.g. using its config or SSH keys).
 - Ignored merge commits with only changes by original author reported.
 - Filtering for SVN to include changes only under source root (and exclude changes under other locations).


### How to install
Download (wget, curl, etc.) from here https://repo1.maven.org/maven2/org/vcsreader/vcsreader 

Maven:
```xml
<dependency>
    <groupId>org.vcsreader</groupId>
    <artifactId>vcsreader</artifactId>
    <version>1.0.0</version>
</dependency>
```
Gradle:
```groovy
repositories {
	mavenCentral()
}
dependencies {
	compile 'org.vcsreader:vcsreader:1.0.0'
} 
```
