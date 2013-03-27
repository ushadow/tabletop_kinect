function hyperParam = hyperparam(paramFromData)
hyperParam.nS = 11;
hyperParam.nXhandFet = 3;
hyperParam.startHandFetNDX = paramFromData.nXconFet + 1;
hyperParam.dir = paramFromData.dir;

nmodel = length(hyperParam.nS);
hyperParam.model = cell(1, nmodel);
for i = 1 : nmodel
  param = paramFromData;
  param.learner = 'ahmm';
  param.nS = hyperParam.nS(i);
  param.nXhandFet = hyperParam.nXhandFet;
  param.dir = hyperParam.dir;
  hyperParam.model{i} = param;
end
end