classdef TestEvalClassification < TestCase
 
methods
  function self = TestEvalClassification(name)
    self = self@TestCase(name);
  end
  
  function testEvalOneFold(self) %#ok<MANU>
    Y.train = {{1 2; 3 4}};
    Y.validate = {{5 6; 7 8}};
    R.train = {{1 2; 3 4}};
    R.validate = {{5 6; 7 8}};
    stat = evalclassification(Y, R, @zerooneloss);
    train = stat('train');
    validate = stat('validate');
    assertTrue(length(train('error')) == 2);
    assertTrue(all(train('error') == 0));
    assertTrue(all(validate('error') == 0));
  end
end
end