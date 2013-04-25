classdef TestAggregateCV < TestCase
methods
  function self = TestAggregateCV(name)
    self = self@TestCase(name);
  end
  
  function testNaN(self) %#ok<MANU>
    nfold = 3;
    R = cell(1, nfold);
    stat = containers.Map();
    validate = containers.Map();
    validate('precision') = 1;
    validate('recall') = 0.5;
    stat('validate') = validate;
    R{1} = stat;
    
    stat = containers.Map();
    validate = containers.Map();
    validate('precision') = NaN;
    validate('recall') = 0.5;
    stat('validate') = validate;
    R{2} = stat;
    
    stat = containers.Map();
    validate = containers.Map();
    validate('precision') = 0.2;
    validate('recall') = NaN;
    stat('validate') = validate;
    R{3} = stat;
    
    cvstat = aggregatecv(R);
    
    assertTrue(isKey(cvstat, 'validate'));
    result = cvstat('validate');
    assertTrue(isKey(result, 'precision-mean'));
    assertTrue(result('precision-mean') == 0.6);
    assertTrue(abs(result('precision-std') - std([1 0.2])) < 1e-9);
    assertTrue(isKey(result, 'precision-std'));
  end
end
end