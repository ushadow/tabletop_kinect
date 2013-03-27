function hyperParam = hyperparam(paramFromData)
hyperParam.nS = 11;
hyperParam.nXhandFet = 3;
hyperParam.startHandFetNDX = paramFromData.nXconFet + 1;

nmodel = length(hyperParam.nS);
hyperParam.model = cell(1, nmodel);
for i = 1 : nmodel
  param = paramFromData;
  param.learner = 'ahmm';
  param.nS = hyperParam.nS(i);
  param.nXhandFet = hyperParam.nXhandFet;
  hyperParam.model{i} = param;
end
end