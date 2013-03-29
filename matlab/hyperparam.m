function hyperParam = hyperparam(paramFromData)
hyperParam.nS = 11;
hyperParam.nhandFet = 3;

hyperParam.startHandFetNDX = paramFromData.nconFet + 1;
hyperParam.dir = paramFromData.dir;
hyperParam.maxIter = 10;

nmodel = length(hyperParam.nS);
hyperParam.model = cell(1, nmodel);
for i = 1 : nmodel
  param = paramFromData;
  param.learner = 'ahmm';
  param.preprocess = {@eigenhand @standardizefeature};
  param.nS = hyperParam.nS(i);
  param.nhandFet = hyperParam.nhandFet;
  param.startHandFetNDX = hyperParam.startHandFetNDX;
  param.dir = hyperParam.dir;
  param.maxIter = hyperParam.maxIter;
  hyperParam.model{i} = param;
end
end