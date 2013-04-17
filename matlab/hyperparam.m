function hyperParam = hyperparam(paramFromData, varargin)

% Default values.
hyperParam.nS = 11;
hyperParam.L = 5;
hyperParam.nhandFet = 3;
hyperParam.cov_type = 'diag';
hyperParam.resetS = false;
hyperParam.inferMethod = 'fixed-interval-smoothing';
hyperParam.learner = @ahmmlearner;
hyperParam.preprocess = {@denoise @eigenhand @standardizefeature};
hyperParam.startHandFetNDX = paramFromData.nconFet + 1;
hyperParam.dir = paramFromData.dir;
hyperParam.maxIter = 100;

for i = 1 : 2 : length(varargin)
  hyperParam.(varargin{i}) = varargin{i + 1};
end

nmodel = length(hyperParam.nS) * length(hyperParam.L);
hyperParam.model = cell(1, nmodel);
for i = 1 : length(hyperParam.nS)
  for j = 1 : length(hyperParam.L)
    param = paramFromData;
    
    param.learner = hyperParam.learner;
    param.inferMethod = hyperParam.inferMethod;
    param.preprocess = hyperParam.preprocess;
    param.nS = hyperParam.nS(i);
    param.L = hyperParam.L(j);
    param.nhandFet = hyperParam.nhandFet;
    param.startHandFetNDX = hyperParam.startHandFetNDX;
    param.dir = hyperParam.dir;
    param.maxIter = hyperParam.maxIter;
    param.cov_type = hyperParam.cov_type;
    param.resetS = hyperParam.resetS;
    
    ndx = (i - 1) * length(hyperParam.L) + j;
    hyperParam.model{ndx} = param;
  end
end
end