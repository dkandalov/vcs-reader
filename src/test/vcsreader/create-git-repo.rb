require 'fileutils.rb'
require 'json'

class GitRepositoryCreator
  def initialize
    @path_to_config = "src/test/vcsreader/test-config.json"
    config = JSON.parse(File.new(@path_to_config).readlines.join("\n"))
    @git = config["pathToGit"]
    @base_dir = config["referenceProject"]
    @author = config["authorWithEmail"]
  end

  def create_reference_repository
    raise("#{@base_dir} already exists") if Dir.exist?(@base_dir)
    FileUtils.mkpath(@base_dir)

    Dir.chdir(@base_dir) do
      puts `#{@git} init`

      puts `echo "file1 content" > file1.txt`
      commit "initial commit", "Aug 10 15:00:00 2014 +0100", @author

      puts `echo "file2 content" > file2.txt`
      puts `echo "file3 content" > file3.txt`
      commit "added file2, file3", "Aug 11 15:00:00 2014 +0100", @author

      puts `echo "file2 new content" > file2.txt`
      puts `echo "file3 new content" > file3.txt`
      commit "modified file2, file3", "Aug 12 15:00:00 2014 +0100", @author

      puts `mkdir folder1`
      puts `mv file1.txt folder1/file1.txt`
      commit "moved file1", "Aug 13 15:00:00 2014 +0100", @author

      puts `mkdir folder2`
      puts `mv folder1/file1.txt folder2/renamed_file1.txt`
      commit "moved and renamed file1", "Aug 14 15:00:00 2014 +0100", @author

      puts `rm folder2/renamed_file1.txt`
      commit "deleted file1", "Aug 15 15:00:00 2014 +0100", @author

      @commit_hashes = log_commit_hashes
    end
    update_test_config(@commit_hashes)
  end

  private

  def commit(message, date, author)
    puts `#{@git} add --all .`
    puts `#{@git} commit -m "#{message}"`
    puts `GIT_COMMITTER_DATE="#{date}" #{@git} commit --amend --author "#{author}" --date "#{date}" -m "#{message}"`
  end

  def replace(pattern, replacement, file_name)
    text = File.read(file_name)
    text = text.gsub(pattern, replacement)
    File.open(file_name, "w") { |file| file.puts text }
  end

  def log_commit_hashes
    log = `#{@git} log`
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

GitRepositoryCreator.new.create_reference_repository


