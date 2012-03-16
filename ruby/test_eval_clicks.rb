#!/usr/bin/env ruby

require './eval_clicks.rb'
require 'test/unit'

class TestEvalClicks < Test::Unit::TestCase
  
  def test_true_pos
    groundtruth = [[1, 0, 0], [2, 0, 0], [3, 0, 0], [4, 0, 0]]
    detected = [[2, 0, 0, 0]]
    result = eval_detected_clicks detected, groundtruth
    assert_equal 1, result[:true_pos]       
  end
end
