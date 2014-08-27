def svn_log
  repo_url = "file:///tmp/reference-repos/svn-repo"
  date_range = "-r {2014-01-01}:{2015-01-01}"
  puts `svn log #{repo_url} #{date_range} --use-merge-history --stop-on-copy --verbose --xml`
end

def svn_show_file_content
  repo_url = "http://svn.apache.org/viewvc/"
  revision = "1610057"
  file = "/commons/proper/collections/trunk/pom.xml"
  puts `svn cat #{repo_url}/#{file}@#{revision}`
end

svn_log
# svn_show_file_content