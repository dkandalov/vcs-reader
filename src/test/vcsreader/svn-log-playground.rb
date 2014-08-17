def svn_log
  repo_url = "http://svn.apache.org/viewvc/commons/proper/collections/trunk"
  date_range = "-r {2014-07-01}:{2014-08-01}"
  puts `svn log #{repo_url} #{date_range} --use-merge-history --stop-on-copy --verbose --xml`
end

def svn_show_file_content
  repo_url = "http://svn.apache.org/viewvc/"
  revision = "1610057"
  file = "/commons/proper/collections/trunk/pom.xml"
  puts `svn cat #{repo_url}/#{file}@#{revision}`
end

# svn_log
svn_show_file_content