#!/usr/bin/env ruby

MAX_INT = 2 ** (0.size * 8 - 1) - 1

# Convert an array to an array of points with the specified dimension.
#
# @param [Array] array of numbers to be grouped into points.
# @param [Fixnum] dimension of a point.
# @return [Array] array of points which are represented in arrays as well.
def to_point_array(array, dim)
  num_points = array.length / dim
  points = Array.new num_points
  (0...num_points).map { |i| array[i * dim ... (i + 1) * dim] } 
end

# Evaluates the error between the detected points and the groundtruth points.
#
# @param [Array] groundtruth points.
# @param [Array] detected points.
# @return [Fixnum] total error.
def eval_error(gt, detected)
  total_error = 0
  gt = Array.new gt 
  detected.each do |dp|
    return total_error if gt.empty?
    dx, dy = dp[0], dp[1]
    min_error = MAX_INT
    min_index = 0
    gt.each_with_index do |gp, i|
      gx, gy = gp[0], gp[1]
      point_error = Math.sqrt (gx - dx) * (gx - dx) + (gy - dy) * (gy - dy)
      if point_error < min_error
        min_error, min_index = point_error, i
      end 
    end
    gt.delete_at min_index
    total_error += min_error
  end
  total_error
end

# Evaluate the accuracy of fingertip detection results.
# 
# @param [Array] array of numbers as groundtruth true labels of fingertips 
#     sorted according to frame IDs.
# @param [Array] detected detected fingertips sorted according to frame IDs. 
# @return [String] result string.
def eval_fingertips(groundtruth, detected)
  gi = di = 0
  false_pos = true_positive = false_neg = 0
  total_groundtruth_fingertips = total_detected_fingertips = 0
  error = 0
  while gi < groundtruth.length && di < detected.length
    gf, df = groundtruth[gi][0], detected[di][0] 
    g_fingertips = to_point_array groundtruth[gi][1..-1], 2
    d_fingertips = to_point_array detected[di][1..-1], 3
    if gf == df 
      error += eval_error g_fingertips, d_fingertips
      total_groundtruth_fingertips += g_fingertips.length
      total_detected_fingertips += d_fingertips.length
      true_positive += [g_fingertips.length, d_fingertips.length].min
      if g_fingertips.length > d_fingertips.length
        false_neg += g_fingertips.length - d_fingertips.length
      else 
        false_pos += d_fingertips.length - g_fingertips.length
      end
      gi += 1
      di += 1
    elsif gf < df
      total_groundtruth_fingertips += g_fingertips.length
      false_neg += g_fingertips.length
      gi += 1
    else # Groundtruth frame id is greater than that of detected frame ID.
      total_detected_fingertips += d_fingertips.length
      false_pos += d_fingertips.length
      di += 1
    end
  end
  
  groundtruth[gi..-1].each do  |l| 
    num_points = to_point_array(l, 2).length 
    false_neg += num_points
    total_groundtruth_fingertips += num_points 
  end 
  
  detected[di..-1].each do |l| 
    num_points = to_point_array(l, 3).length
    false_pos += num_points
    total_detected_fingertips += num_points  
  end
  
  error /= true_positive
  result = <<EOS
total fingertips in groundtruth: #{total_groundtruth_fingertips}
total fingertips in detected: #{total_detected_fingertips}
true positives: #{true_positive}
error for true positives: #{error}
false positives: #{false_pos}
false negatives: #{false_neg}
EOS
      
end

if __FILE__ == $0
  groundtruth_file = ARGV[0]
  groundtruth = File.read(groundtruth_file).split("\n")[1..-1]
  groundtruth.map! { |l| l.split.map(&:to_i) }
    
  detected_file = ARGV[1]
  detected = File.read(detected_file).split("\n")[1..-1]
  detected.map! { |l| l.split.map(&:to_i) }
  
  puts eval_fingertips groundtruth, detected
end