require 'fileutils.rb'

base_dir = "/tmp/test-repos/git-repo"
raise("#{base_dir} already exists") if Dir.exist?(base_dir)
FileUtils.mkpath(base_dir)

Dir.chdir(base_dir) do
  puts `git init`

  puts `echo "file1 content" > file1.txt`
  puts `git add .`
  puts `git commit -m "initial commit"`
  date = "Sun Aug 10 15:00:00 2014 +0100"
  puts `GIT_COMMITTER_DATE="#{date}" git commit --amend --date "#{date}" -m "initial commit"`

  puts `echo "file2 content" > file2.txt`
  puts `git add .`
  puts `git commit -m "added file2.txt"`
  date = "Sun Aug 11 15:00:00 2014 +0100"
  puts `GIT_COMMITTER_DATE="#{date}" git commit --amend --date "#{date}" -m "added file2.txt"`

  log = `git log`
  commit_hashes = log.split("\n").
      delete_if {|line| not line.include?("commit ")}.
      collect { |line| line.gsub(/commit /, "") }
  puts commit_hashes
end

