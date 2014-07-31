require 'fileutils.rb'

base_dir = "/tmp/test-repos/git-repo"
raise("#{base_dir} already exists") if Dir.exist?(base_dir)
FileUtils.mkpath(base_dir)

Dir.chdir(base_dir) do
  puts `git init`
  puts `echo abc > file1.txt`
  puts `git add .`
  puts `git commit -m "initial commit"`
end

