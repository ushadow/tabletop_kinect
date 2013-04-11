function hyperParam = hyperparam(paramFromData, varargin)

% Default values.
hyperParam.nS = 11;
hyperParam.nhandFet = 3;
hyperParam.cov_type = 'diag';
hyperParam.resetS = true;

for i = 1 : 2 : length(varargin)
  switch varargin{i}
    case 'nS'
      hyperParam.nS = varargin{i + 1};
    case 'resetS'
      hyperParam.resetS = varargin{i + 1};
    otherwise
      error(['Unrecognized parameter: ' varargin{i}]);
  end
end


hyperParam.startHandFetNDX = paramFromData.nconFet + 1;
hyperParam.dir = paramFromData.dir;
hyperParam.maxIter = 100;

nmodel = length(hyperParam.nS);
hyperParam.model = cell(1, nmodel);
for i = 1 : nmodel
  param = paramFromData;
  param.learner = 'ahmm';
  param.preprocess = {@denoise @eigenhand @standardizefeature};
  param.nS = hyperParam.nS(i);
  param.nhandFet = hyperParam.nhandFet;
  param.startHandFetNDX = hyperParam.startHandFetNDX;
  param.dir = hyperParam.dir;
  param.maxIter = hyperParam.maxIter;
  param.cov_type = hyperParam.cov_type;
  param.resetS = hyperParam.resetS;
  hyperParam.model{i} = param;
end
end