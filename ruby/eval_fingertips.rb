#!/usr/bin/env ruby

# Evaluate the accuracy of fingertip detection results.
# 
# @param [Array] groundtruth true labels of fingertips sorted according to frame 
#     IDs.
# @param [Array] detected detected fingertips sorted according to frame IDs. 
# @return [String] result string.
def eval_fingertips(groundtruth, detected)
  gi = di = 0
  false_positive = true_positive = false_negative = 0
  error = 0
  while gi < groundtruth.length && di < detected.length
    gf, df = groundtruth[gi][0], detected[di][0] 
    if gf == df 
      gx, gy = groundtruth[gi][1].to_i, groundtruth[gi][2].to_i
      dx, dy = detected[di][1].to_i, detected[di][1].to_i
      error += Math.sqrt((gx - dx) * (gx - dx) + (gy - dy) * (gy - dy))
      true_positive += 1
      gi += 1
      di += 1
    elsif gf < df
      false_negative += 1
      gi += 1
    else # Groundtruth frame id is greater than that of detected frame ID.
      false_positive += 1
      di += 1
    end
  end
  if gi < groundtruth.length
    false_negative += groundtruth.length - gi
  elsif di < detected.length
    false_positive += detected.length - di
  end
  
  error /= true_positive
  result = <<EOS
total fingertips in groundtruth: #{groundtruth.length}
total fingertips in detected: #{detected.length}
true positives: #{true_positive}
error for true positives: #{error}
false positives: #{false_positive}
false negatives: #{false_negative}
EOS
      
end

if __FILE__ == $0
  groundtruth_file = ARGV[0]
  detected_file = ARGV[1]
  groundtruth = File.read(groundtruth_file).split("\n")[1..-1]
  detected = File.read(detected_file).split("\n")[1..-1]
  puts eval_fingertips groundtruth, detected
end