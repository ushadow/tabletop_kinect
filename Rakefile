#!usr/bin/env rake

task :cluster do |t|
  dir = 'data/descriptor'
  input = File.join dir, 'pose-descriptor.txt'
  output = File.join dir, 'clusters.txt'
  sh "R --no-save --slave --args #{input} #{output} < r/run_cluster.R" 
end
