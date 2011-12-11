#!/usr/bin/env ruby

require './eval_fingertips.rb'
require 'test/unit'

class TestEvalFingertips < Test::Unit::TestCase
  
  def test_all_positive
    groundtruth = [[1, 0, 0, 0]]
    detected = [[1, 0, 0, 0]]
    result = eval_fingertips groundtruth, detected
    assert_equal 0, result[:error]
    assert_equal 1, result[:true_pos]
  end
end
