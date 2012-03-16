#!/usr/bin/env ruby

require 'optparse'

def eval_segment(segment)
  avgx = segment.reduce(0) { |sum, e| sum + e[1] } / segment.size
  avgy = segment.reduce(0) { |sum, e| sum + e[2] } / segment.size
  {start_index: segment.first[0], end_index: segment.last[0], avgx: avgx, 
   avgy: avgy}    
end

def eval_gt_clicks(groundtruth)
  segment = [groundtruth.first]
  clicks = []
  groundtruth[1..-1].each do |e|
    if e[0] > segment.last[0] + 1
      clicks << eval_segment(segment)
      segment = [e]
    else 
      segment << e   
    end
  end
  clicks << eval_segment(segment)
end

# Evaluates the accurarcy of the detected clicks.
# @param [Array] detected detected fingertips sorted according to frame IDs. 
# @param [Array] groundtruth each element is an array of numbers as groundtruth 
#   true labels of fingertips sorted according to frame IDs.
def eval_detected_clicks(detected, groundtruth)
  gt_clicks = eval_gt_clicks groundtruth
  puts "Number of ground truth clicks: #{gt_clicks.size}"
  cursor = 0
  false_pos = true_pos = false_neg = 0 
  false_neg_duration = total_false_neg_duration = total_segment = 0
  
  gt_clicks.each do |click|
    start_index = click[:start_index]
    end_index = click[:end_index]
    prev = -1
    while cursor < detected.size && detected[cursor][0] < start_index
      false_pos += 1
      
      if prev != -1
        if detected[cursor][0] == prev + 1
          false_neg_duration += 1
        else
          total_false_neg_duration += false_neg_duration
          total_segment +=1
          prev = -1
          false_neg_duration = 0
        end
      end
      
      prev = detected[cursor][0]
      
      puts "false positive: #{detected[cursor][0]}, " +  
           "start index = #{start_index}"
      cursor += 1
    end
    if cursor < detected.size && 
       (start_index..end_index) === detected[cursor][0]
      true_pos += 1
      cursor += 1
      while cursor < detected.size &&  
            (start_index..end_index) === detected[cursor][0]
        cursor += 1
      end
    else 
      false_neg += 1
      puts "false negative: #{start_index} - #{end_index}"
    end
  end
  {true_pos: true_pos, false_pos: false_pos, false_neg: false_neg, 
   false_neg_duration: Float(total_false_neg_duration) / total_segment}
end

option_parser = OptionParser.new do |opts|
  executable_name = File.basename($0)
  opts.banner = "Usage: #{executable_name} groundtruth_file detected_file"
end

if __FILE__ == $0
  option_parser.parse!
  
  groundtruth_file = ARGV[0]
  groundtruth = File.read(groundtruth_file).split("\n")[1..-1]
  groundtruth.map! { |l| l.split.map(&:to_i) }
    
  detected_file = ARGV[1]
  detected = File.read(detected_file).split("\n")[1..-1]
  detected.map! { |l| l.split.map(&:to_i) }
  
  result = eval_detected_clicks detected, groundtruth
  puts <<EOS
true positives: #{result[:true_pos]}
false positivies: #{result[:false_pos]}
false negative : #{result[:false_neg]}
false negative duration: #{result[:false_neg_duration]}
EOS
end
