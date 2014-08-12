require 'fileutils.rb'

base_dir = "/tmp/test-repos/git-repo"
raise("#{base_dir} already exists") if Dir.exist?(base_dir)
FileUtils.mkpath(base_dir)

def commit(message, date, author)
  puts `git add .`
  puts `git commit -m "#{message}"`
  puts `GIT_COMMITTER_DATE="#{date}" git commit --amend --author "#{author}" --date "#{date}" -m "#{message}"`
end

def print_commit_hashes
  log = `git log`
  commit_hashes = log.split("\n").
      delete_if { |line| not line.include?("commit ") }.
      collect { |line| line.gsub(/commit /, "") }
  puts commit_hashes
end

author = "Some Author <some.author@mail.com>"
Dir.chdir(base_dir) do
  puts `git init`

  puts `echo "file1 content" > file1.txt`
  commit("initial commit", "Sun Aug 10 15:00:00 2014 +0100", author)

  puts `echo "file2 content" > file2.txt`
  puts `echo "file3 content" > file3.txt`
  commit("added file2, file3", "Sun Aug 11 15:00:00 2014 +0100", author)

  print_commit_hashes
end

