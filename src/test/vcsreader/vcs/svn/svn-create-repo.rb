require 'fileutils.rb'
require 'json'

class SvnRepositoryCreator
  def initialize
    @path_to_config = "src/test/vcsreader/vcs/svn/svn-test-config.json"
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
      add "file1.txt"
      commit "initial commit", "2014-08-10T15:00:00.000000Z", @author

      puts `echo "file2 content" > file2.txt`
      puts `echo "file3 content" > file3.txt`
      add "file2.txt", "file3.txt"
      commit "added file2, file3", "2014-08-11T15:00:00.000000Z", @author

      puts `echo "file2 new content" > file2.txt`
      puts `echo "file3 new content" > file3.txt`
      commit "modified file2, file3", "2014-08-12T15:00:00.000000Z", @author

      mkdir "folder1"
      move "file1.txt", "folder1/file1.txt"
      commit "moved file1", "2014-08-13T15:00:00.000000Z", @author

      mkdir "folder2"
      move "folder1/file1.txt", "folder2/renamed_file1.txt"
      puts `echo "file2 new new content" > file2.txt`
      commit "moved and renamed file1", "2014-08-14T15:00:00.000000Z", @author

      rm "folder2/renamed_file1.txt"
      commit "deleted file1", "2014-08-15T15:00:00.000000Z", @author

      puts `echo 123 > "\\"file with spaces.txt\\""`
      add "\\\"file with spaces.txt\\\""
      commit "added file with spaces and quotes", "2014-08-16T15:00:00.000000Z", @author
    end

    update_test_config
  end

  private

  def create_dummy_hook(file_path)
    puts `echo "#!/bin/sh\nexit 0" > #{file_path}`
    puts `chmod +x #{file_path}`
  end

  def add(*files)
    files.each do |it|
      puts `#{@svn} add "#{it}"`
    end
  end

  def move(from, to)
    puts `#{@svn} move #{from} #{to}`
  end

  def mkdir(dir)
    puts `#{@svn} mkdir #{dir}`
  end

  def rm(file_name)
    puts `#{@svn} rm #{file_name}`
  end

  def commit(message, date, author)
    puts `#{@svn} commit -m "#{message}"`
    puts `#{@svn} propset svn:date --revprop -r HEAD #{date}`
    puts `#{@svn} propset svn:author --revprop -r HEAD "#{author}"`
    @revisions ||= 0
    @revisions = @revisions + 1
  end

  def update_test_config
    revisions_literal = (1..@revisions).to_a.join(",")
    replace(
        /"revisions": \[.*\]/,
        "\"revisions\": [#{revisions_literal}]",
        @path_to_config
    )
  end

  def replace(pattern, replacement, file_name)
    text = File.read(file_name)
    text = text.gsub(pattern, replacement)
    File.open(file_name, "w") { |file| file.puts text }
  end
end

SvnRepositoryCreator.new.create_reference_repository


