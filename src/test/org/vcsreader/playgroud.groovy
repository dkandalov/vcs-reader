package org.vcsreader

import org.vcsreader.vcs.git.GitSettings
import org.vcsreader.vcs.git.GitVcsRoot

import static org.vcsreader.VcsChange.Type.MODIFIED
import static org.vcsreader.lang.DateTimeUtil.dateTime
import static org.vcsreader.lang.DateTimeUtil.timeRange

// setup project
def gitSettings = GitSettings.defaults()
def vcsRoot = new GitVcsRoot("/tmp/junit", "https://github.com/junit-team/junit", gitSettings)
def vcsProject = new VcsProject(vcsRoot)

// clone from GitHub (cloning is optional, already clone project can be used)
def cloneResult = vcsProject.cloneToLocal()
assert cloneResult.isSuccessful()

// log commits within date range
def logResult = vcsProject.log(timeRange("01/01/2013", "01/01/2014"))
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
