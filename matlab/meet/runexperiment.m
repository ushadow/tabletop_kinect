function R = runexperiment(param, split, data)    
% R = runexperiment(param, split, data)
%
% Input:
%   - param : struct, model parameters
%   - split  : 2-by-1 or 3-by-1 cell array
%
% Output:
%   - R      : struct, result

  R.split = split;
  R.param = param;
  param.userId = data.userId;
  
  % Step 1: Prepare data
  if exist('data','var')
    Y.train = data.Y(split{1});
    Y.validate = data.Y(split{2}); 

    X.train = data.X(split{1});
    X.validate = data.X(split{2});

    if ~isempty(split{3})
      Y.test  = data.Y(split{3}); 
      X.test  = data.X(split{3});
    end

  else
    job = getCurrentJob();    
    Y.train = job.JobData.Y(split{1});
    Y.validate = job.JobData.Y(split{2});
    Y.test  = job.JobData.Y(split{3});
    X.train = job.JobData.X(split{1});
    X.validate = job.JobData.X(split{2});
    X.test  = job.JobData.X(split{3});
  end

  % Step 2: Preprocess data (optional)
  %   Dimensionality reduction, standardzation, sparsification
  if isfield(param, 'preprocess')
    for i = 1 : length(param.preprocess)
      preprocess = param.preprocess{i};
      X = preprocess(X, param);
    end
  end

  % Step 3: Train and test model, get prediction on all three splits
  if isfield(param, 'learner')
    [R.prediction, R.learnedModel] = param.learner(Y, X, param);
  end

  % Step 5: Evaluate performance of prediction
  R.stat = evalclassification(Y, R.prediction, @errorperframe);
end


