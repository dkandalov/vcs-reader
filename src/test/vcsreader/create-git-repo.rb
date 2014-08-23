require 'fileutils.rb'
require 'json'

def commit(message, date, author)
  puts `#{GIT} add --all .`
  puts `#{GIT} commit -m "#{message}"`
  puts `GIT_COMMITTER_DATE="#{date}" #{GIT} commit --amend --author "#{author}" --date "#{date}" -m "#{message}"`
end

def replace(pattern, replacement, file_name)
  text = File.read(file_name)
  text = text.gsub(pattern, replacement)
  File.open(file_name, "w") { |file| file.puts text }
end

def log_commit_hashes
  log = `#{GIT} log`
  log.split("\n").
      delete_if { |line| not line.include?("commit ") }.
      collect { |line| line.gsub(/commit /, "") }.
      reverse
end

def update_test_config(commit_hashes)
  hashes_literal = commit_hashes.collect { |hash| '"' + hash + '"' }.join(",")
  replace(
      /"revisions": \[.*\]/,
      "\"revisions\": [#{hashes_literal}]",
      "test-config.json"
  )
end

config = JSON.parse(File.new("test-config.json").readlines.join("\n"))
GIT = config["pathToGit"]
BASE_DIR = config["referenceProject"]
author = config["authorWithEmail"]
commit_hashes = []


raise("#{BASE_DIR} already exists") if Dir.exist?(BASE_DIR)
FileUtils.mkpath(BASE_DIR)

Dir.chdir(BASE_DIR) do
  puts `#{GIT} init`

  puts `echo "file1 content" > file1.txt`
  commit "initial commit", "Aug 10 15:00:00 2014 +0100", author

  puts `echo "file2 content" > file2.txt`
  puts `echo "file3 content" > file3.txt`
  commit "added file2, file3", "Aug 11 15:00:00 2014 +0100", author

  puts `echo "file2 new content" > file2.txt`
  puts `echo "file3 new content" > file3.txt`
  commit "modified file2, file3", "Aug 12 15:00:00 2014 +0100", author

  puts `mkdir folder1`
  puts `mv file1.txt folder1/file1.txt`
  commit "moved file1", "Aug 13 15:00:00 2014 +0100", author

  puts `mkdir folder2`
  puts `mv folder1/file1.txt folder2/renamed_file1.txt`
  commit "moved and renamed file1", "Aug 14 15:00:00 2014 +0100", author

  puts `rm folder2/renamed_file1.txt`
  commit "deleted file1", "Aug 15 15:00:00 2014 +0100", author

  commit_hashes = log_commit_hashes
end

update_test_config(commit_hashes)