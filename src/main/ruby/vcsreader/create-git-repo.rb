require 'fileutils.rb'

base_dir = "/tmp/test-repos/git-repo"
raise("#{base_dir} already exists") if Dir.exist?(base_dir)
FileUtils.mkpath(base_dir)

def commit(message, date, author)
  puts `git add .`
  puts `git commit -m "#{message}"`
  puts `GIT_COMMITTER_DATE="#{date}" git commit --amend --author "#{author}" --date "#{date}" -m "#{message}"`
end

def replace(pattern, replacement, in_file)
  text = File.read(in_file)
  text = text.gsub(pattern, replacement)
  File.open(in_file, "w") { |file| file.puts text }
end

def commit_hashes
  log = `git log`
  log.split("\n").
      delete_if { |line| not line.include?("commit ") }.
      collect { |line| line.gsub(/commit /, "") }.
      reverse
end

def update_test_config
  hashes_literal = commit_hashes.collect { |hash| '"' + hash + '"' }.join(",")
  replace(
      /def revisions = \[.*\]/,
      "def revisions = [#{hashes_literal}]",
      "/Users/dima/IdeaProjects/vcs-reader/test/main/groovy/vcsreader/vcs/IntegrationTestConfig.groovy"
  )
end

author = "Some Author <some.author@mail.com>"

Dir.chdir(base_dir) do
  puts `git init`

  puts `echo "file1 content" > file1.txt`
  commit("initial commit", "Sun Aug 10 15:00:00 2014 +0100", author)

  puts `echo "file2 content" > file2.txt`
  puts `echo "file3 content" > file3.txt`
  commit("added file2, file3", "Sun Aug 11 15:00:00 2014 +0100", author)

  puts `echo "file2 new content" > file2.txt`
  puts `echo "file3 new content" > file3.txt`
  commit("modified file2, file3", "Sun Aug 12 15:00:00 2014 +0100", author)

  update_test_config
end

