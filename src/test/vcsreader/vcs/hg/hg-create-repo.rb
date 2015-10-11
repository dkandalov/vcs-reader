require 'fileutils.rb'
require 'json'

class HgRepositoryCreator
  def initialize
    @path_to_config = "src/test/vcsreader/vcs/hg/hg-test-config.json"
    config = JSON.parse(File.new(@path_to_config).readlines.join("\n"))
    @hg = config["pathToHg"]
    @base_dir = config["referenceProject"]
    @author = config["authorWithEmail"]
  end

  def create_reference_repository
    FileUtils.rmtree(@base_dir)
    FileUtils.mkpath(@base_dir)

    Dir.chdir(@base_dir) do
      puts `#{@hg} init`

      puts `echo "file1 content" > file1.txt`
      commit "initial commit", "Aug 10 14:00:00 2014 +0000", @author

      puts `echo "file2 content" > file2.txt`
      puts `echo "file3 content" > file3.txt`
      commit "added file2, file3", "Aug 11 14:00:00 2014 +0000", @author

      puts `echo "file2 new content" > file2.txt`
      puts `echo "file3 new content" > file3.txt`
      commit "modified file2, file3", "Aug 12 14:00:00 2014 +0000", @author

      puts `mkdir folder1`
      move "file1.txt", "folder1/file1.txt"
      commit "moved file1", "Aug 13 14:00:00 2014 +0000", @author

      puts `mkdir folder2`
      move "folder1/file1.txt", "folder2/renamed_file1.txt"
      commit "moved and renamed file1", "Aug 14 14:00:00 2014 +0000", @author

      rm "folder2/renamed_file1.txt"
      commit "deleted file1", "Aug 15 14:00:00 2014 +0000", @author

      puts `echo 123 > "\\"file with spaces.txt\\""`
      commit "added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000", @author

      puts `echo "non-ascii содержимое" > "non-ascii.txt"`
      commit "non-ascii комментарий", "Aug 17 15:00:00 2014 +0000", @author

      # Mercurial doesn't seem to support empty commits and empty commit messages
      # puts `echo "commit with no comment" > file4.txt`
      # commit "", "Aug 18 16:00:00 2014 +0000", @author
      # commit "commit with no changes", "Aug 19 17:00:00 2014 +0000", @author

      @commit_hashes = log_commit_hashes
    end
    update_test_config(@commit_hashes)
  end

  private

  def rm(file)
    puts `#{@hg} rm #{file}`
  end

  def move(from, to)
    puts `#{@hg} mv #{from} #{to}`
  end

  def commit(message, date, author)
    puts `#{@hg} add`
    puts `#{@hg} commit -d '#{date}' -u '#{author}' -m "#{message}"`
  end

  def log_commit_hashes
    log = `#{@hg} log --template "{node}\n"`
    log.split("\n").reverse
  end

  def update_test_config(commit_hashes)
    hashes_literal = commit_hashes.collect { |hash| '"' + hash + '"' }.join(",")
    replace(
        /"revisions": \[.*\]/m,
        "\"revisions\": [#{hashes_literal}]",
        @path_to_config
    )
  end

  def replace(pattern, replacement, file_name)
    text = File.read(file_name)
    text = text.gsub(pattern, replacement)
    File.open(file_name, "w") { |file| file.puts text }
  end
end

HgRepositoryCreator.new.create_reference_repository


