function R = runexperiment(param, split, data)    
% R = run_experiment( params, split )
%
% Input:
%   - params : struct, model parameters
%   - split  : 2-by-1 or 3-by-1 cell array
%
% Output:
%   - R      : struct, result

  R.split = split;
  
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
  switch param.learner
    case 'svr'
      R = learn_svr(Y, X, param);
    case 'ahmm'
      R.prediction = learnahmm(Y, X, param);
    otherwise
        error('%s Not implemented yet', param.model);
  end

  % Step 4: Postprocess prediction result (optional)
  if isfield(param, 'postprocess')
      switch param.postprocess
        case 'exp_smooth'
          R.prediction = post_exp_smooth(R.prediction, ...
                                         param.exp_smooth_alpha);
      end
  end

  % Step 5: Evaluate performance of prediction
  switch param.learner
    case 'svr'
      R.stat = eval_svr(Y, R.prediction, param);
    case 'ahmm'
      R.stat = evalclassification(Y, R.prediction);
    otherwise
      error('%s Not implemented yet', param.model);
  end

end


