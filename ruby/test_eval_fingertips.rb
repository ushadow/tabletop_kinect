#!/usr/bin/env ruby

require './eval_fingertips.rb'
require 'test/unit'

class TestEvalFingertips < Test::Unit::TestCase
  
  def test_one_point
    groundtruth = [[1, 0, 0]]
    detected = [[1, 0, 0, 0]]
    result = eval_fingertips groundtruth, detected
    assert_equal 0, result[:error]
    assert_equal 1, result[:true_pos]
  end
  
  def test_two_detected
    groundtruth = [[1, 0, 0]]
    detected = [[1, 0, 0, 0, 1, 1, 1]]
    result = eval_fingertips groundtruth, detected
    assert_equal 0, result[:error]
    assert_equal 1, result[:true_pos]
    
    groundtruth = [[1, 1, 1]]
    detected = [[1, 0, 0, 0, 3, 3, 3]]
    result = eval_fingertips groundtruth, detected
    assert_equal Math.sqrt(2), result[:error]
    assert_equal 1, result[:true_pos]
  end
  
  def test_multiple_points
    groundtruth = [[1, 0, 0], [2, 0, 0]]
    detected = [[1, 2, 2, 2, 0, 0, 0, 1, 1, 1], [3, 0, 0]]
    result = eval_fingertips groundtruth, detected
    assert_equal 0, result[:error]
    assert_equal 1, result[:true_pos]
  end
end
