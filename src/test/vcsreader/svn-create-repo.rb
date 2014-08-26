require 'fileutils.rb'
require 'json'

class SvnRepositoryCreator
  def initialize
    @path_to_config = "svn-test-config.json"
    config = JSON.parse(File.new(@path_to_config).readlines.join("\n"))
    @svn = config["pathToSvn"]
    @svn_admin = config["pathToSvnAdmin"]
    @repo_path = config["svnRepository"]
    @project_path = config["referenceProject"]
    @author = config["author"]
  end

  def create_reference_repository(clean_run = true)
    [@repo_path, @project_path].each { |it|
      FileUtils.rm_rf(it) if clean_run
      raise("#{it} already exists") if Dir.exist?(it)
      FileUtils.mkpath(it)
    }

    puts `#{@svn_admin} create #{@repo_path}`
    create_dummy_hook("#{@repo_path}/hooks/pre-revprop-change")
    puts `#{@svn} checkout file://#{@repo_path} #{@project_path}`

    Dir.chdir(@project_path) do
      puts `echo "file1 content" > file1.txt`
      commit "initial commit", "2009-02-12T00:44:04.921324Z", @author

      # puts `echo "file2 content" > file2.txt`
      # puts `echo "file3 content" > file3.txt`
      # commit "added file2, file3", "Aug 11 15:00:00 2014 +0100", @author
      #
      # puts `echo "file2 new content" > file2.txt`
      # puts `echo "file3 new content" > file3.txt`
      # commit "modified file2, file3", "Aug 12 15:00:00 2014 +0100", @author
      #
      # puts `mkdir folder1`
      # puts `mv file1.txt folder1/file1.txt`
      # commit "moved file1", "Aug 13 15:00:00 2014 +0100", @author
      #
      # puts `mkdir folder2`
      # puts `mv folder1/file1.txt folder2/renamed_file1.txt`
      # commit "moved and renamed file1", "Aug 14 15:00:00 2014 +0100", @author
      #
      # puts `rm folder2/renamed_file1.txt`
      # commit "deleted file1", "Aug 15 15:00:00 2014 +0100", @author
      #
      # @commit_hashes = log_commit_hashes
    end

    # update_test_config(@commit_hashes)
  end

  private

  def create_dummy_hook(file_path)
    puts `echo "#!/bin/sh\nexit 0" > #{file_path}`
  end

  def commit(message, date, author)
    puts `#{@svn} add *`
    puts `#{@svn} commit -m "#{message}"`
    puts `#{@svn} propset svn:date --revprop -r HEAD #{date}`
  end

  def replace(pattern, replacement, file_name)
    text = File.read(file_name)
    text = text.gsub(pattern, replacement)
    File.open(file_name, "w") { |file| file.puts text }
  end

  def log_commit_hashes
    log = `#{@svn} log`
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
        @path_to_config
    )
  end
end

SvnRepositoryCreator.new.create_reference_repository


