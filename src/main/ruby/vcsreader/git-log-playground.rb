
def git_clone
  from_url = "https://github.com/junit-team/junit"
  to_folder = "/tmp/junit-test"
  puts `git clone -v #{from_url} #{to_folder}`
end

def log_format
  # see "PRETTY FORMATS" at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
  committer_date = "%ct"
  commit_hash = "%H"
  author_name = "%an" # see http://stackoverflow.com/questions/18750808/difference-between-author-and-committer-in-git
  committer_name = "%cn"
  subject = "%s"
  body = "%b"
  committed_changelist_format =
      "#{committer_date}%n" +
      "#{commit_hash}%n" +
      "#{author_name}%x20#{committer_name}%n" +
      "#{subject}#{body}%n"
  "--pretty=format:" + committed_changelist_format
end

def git_log
  folder = "/tmp/junit-test"
  Dir.chdir(folder) do
    dates_range = "--after={2014-04-01} --before={2014-04-24}"
    show_file_status = "--name-status" # see --diff-filter at https://www.kernel.org/pub/software/scm/git/docs/git-log.html
    puts `git log #{log_format} #{dates_range} #{show_file_status}`
  end
end

def git_show_file_content
  folder = "/tmp/junit-test"
  revision = "d1b8c04e54f0b9e4807fa88a2ff36ad9ba177107"
  file = "pom.xml"
  Dir.chdir(folder) do
    puts `git show #{revision}:#{file}`
  end
end

# git_log
# git_show_file_content