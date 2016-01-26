require 'fileutils.rb'
require 'json'

class GitRepositoryCreator
	def initialize
		@path_to_config = "src/test/vcsreader/vcs/git/git-test-config.json"
		config = JSON.parse(File.new(@path_to_config).readlines.join("\n"))
		@git = config["pathToGit"]
		@base_dir = config["referenceProject"]
		@author = config["authorWithEmail"]
	end

	def create_reference_repository
		FileUtils.rmtree(@base_dir)
		FileUtils.mkpath(@base_dir)

		Dir.chdir(@base_dir) do
			puts `#{@git} init`

			puts `echo "file1 content" > file1.txt`
			commit "initial commit", "Aug 10 00:00:00 2014 +0000", @author

			puts `echo "file2 content" > file2.txt`
			puts `echo "file3 content" > file3.txt`
			commit "added file2, file3", "Aug 11 00:00:00 2014 +0000", @author

			puts `echo "file2 new content" > file2.txt`
			puts `echo "file3 new content" > file3.txt`
			commit "modified file2, file3", "Aug 12 14:00:00 2014 +0000", @author

			puts `mkdir folder1`
			puts `mv file1.txt folder1/file1.txt`
			commit "moved file1", "Aug 13 14:00:00 2014 +0000", @author

			puts `mkdir folder2`
			puts `mv folder1/file1.txt folder2/renamed_file1.txt`
			commit "moved and renamed file1", "Aug 14 14:00:00 2014 +0000", @author

			puts `rm folder2/renamed_file1.txt`
			commit "deleted file1", "Aug 15 14:00:00 2014 +0000", @author

			puts `echo 123 > "\\"file with spaces.txt\\""`
			commit "added file with spaces and quotes", "Aug 16 14:00:00 2014 +0000", @author

			puts `echo "non-ascii содержимое" > "non-ascii.txt"`
			commit "non-ascii комментарий", "Aug 17 15:00:00 2014 +0000", @author

			puts `echo "commit with no message" > file4.txt`
			commit "", "Aug 18 16:00:00 2014 +0000", @author

			commit "commit with no changes", "Aug 19 17:00:00 2014 +0000", @author

			### branch rebase and merge
			create_branch "a-branch"
			checkout_branch "a-branch"
			puts `echo "file1 branch content" > file1-branch.txt`
			commit "added file1-branch.txt", "Aug 20 18:00:00 2014 +0000", @author

			checkout_branch "master"
			puts `echo "file1-master content" > file1-master.txt`
			commit "added file1-master.txt", "Aug 20 18:10:00 2014 +0000", @author

			checkout_branch "a-branch"
			# simulate what happens on rebase when commit date changes but author date doesn't
			rebase "master", "added file1-branch.txt", "Aug 20 18:20:00 2014 +0000"
			puts `echo "file2 branch content" > file2-branch.txt`
			commit "added file2-branch.txt", "Aug 21 19:00:00 2014 +0000", @author

			checkout_branch "master"
			puts `echo "file2-master content" > file2-master.txt`
			commit "added file2-master.txt", "Aug 21 19:10:00 2014 +0000", @author
			merge_branch "a-branch", "merged branch into master", "Aug 21 19:20:00 2014 +0000", @author
			### end of branch rebase and merge

			@commit_hashes = log_commit_hashes
		end
		update_test_config(@commit_hashes)
	end

	private

	def commit(message, date, author)
		args = "--allow-empty-message --allow-empty" # this is to test commits with empty message and no changes
		puts `#{@git} add --all .`
		puts `#{@git} commit #{args} -m "#{message}"`
		puts `GIT_COMMITTER_DATE="#{date}" #{@git} commit #{args} --amend --author "#{author}" --date "#{date}" -m "#{message}"`
	end

	def create_branch(branch_name)
		puts `#{@git} branch #{branch_name}`
	end

	def checkout_branch(branch_name)
		puts `#{@git} checkout #{branch_name}`
	end

	def rebase(branch_name, message, commit_date)
		puts `#{@git} rebase #{branch_name}`
		puts `GIT_COMMITTER_DATE="#{commit_date}" #{@git} commit --amend -m "#{message}"`
	end

	def merge_branch(branch_name, message, date, author)
		puts `#{@git} merge #{branch_name} -m "#{message}"`
		puts `GIT_COMMITTER_DATE="#{date}" #{@git} commit --amend --author "#{author}" --date "#{date}" -m "#{message}"`
	end

	def log_commit_hashes
		log = `#{@git} log`
		log.split("\n").
				delete_if { |line| not line.start_with?("commit ") }.
				collect { |line| line.gsub(/commit /, "") }.
				reverse
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

GitRepositoryCreator.new.create_reference_repository


