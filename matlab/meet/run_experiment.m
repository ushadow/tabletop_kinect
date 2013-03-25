function R = run_experiment( params, split, data )    
% R = run_experiment( params, split )
%
% Input:
%   - params : struct, model parameters
%   - split  : 2-by-1 or 3-by-1 cell array
%
% Output:
%   - R      : struct, result

    % Step 1: Prepare data
    if exist('data','var')
        Y.train = data.Y(split{1});
        Y.devel = data.Y(split{2}); 
        Y.test  = data.Y(split{3}); 
        X.train = data.X(split{1});
        X.devel = data.X(split{2});
        X.test  = data.X(split{3});
    else
        job = getCurrentJob();    
        Y.train = job.JobData.Y(split{1});
        Y.devel = job.JobData.Y(split{2});
        Y.test  = job.JobData.Y(split{3});
        X.train = job.JobData.X(split{1});
        X.devel = job.JobData.X(split{2});
        X.test  = job.JobData.X(split{3});
    end
     
    % Step 2: Preprocess data (optional)
    %   Dimensionality reduction, standardzation, sparsification
    if isfield(params,'preprocess')
        switch params.preprocess
            case 'pca'
                X = pre_pca( X, params );
        end
    end
        
    % Step 3: Train and test model, get prediction on all three splits
    switch params.learner
        case 'svr'
            R = learn_svr( Y, X, params );
        otherwise
            error('%s Not implemented yet', params.model);
    end
    
    % Step 4: Postprocess prediction result (optional)
    if isfield(params,'postprocess')
        switch params.postprocess
            case 'exp_smooth'
                R.prediction = post_exp_smooth( R.prediction, params.exp_smooth_alpha );
        end
    end
    
    % Step 5: Evaluate performance of prediction
    switch params.learner
        case 'svr'
            R.stat = eval_svr( Y, R.prediction, params );
        otherwise
            error('%s Not implemented yet', params.model);
    end
    
end


