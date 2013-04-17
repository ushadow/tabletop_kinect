function R = runexperimentparallel(data, modelParam, jobParam)
% RUNEXPERIMENTPARALLEL runs experiment for one batch of data in parallel.
%
% Args
% - modelParam: a cell array of model parameter.
    verbose = jobParam.verbose;
    
    switch jobParam.type
        case 'local'
            jm = findResource('scheduler','type','local');
            job = createJob(jm);
            distributed = true;
        case 'none'
            distributed = false;
    end
    
    split = data.split;
    R = cell(numel(modelParam), size(split, 2));    
    nargout = 1; % to be returned from each task 
 
    % Generate tasks
    if verbose, fprintf('Generate tasks'); tid=tic(); end
    num_tasks = 1;
    for model = 1 : numel(modelParam) % for each model (row)
        params = modelParam{model};
        for fold = 1 : size(split, 2) % for each fold (col)
          params.fold = fold;
          if verbose, fprintf('.'); end  
          if distributed
            createTask(job, @runexperiment, nargout, {params, split(:, fold), data});
          else
            R{model, fold} = runexperiment(params, split(:, fold), data);
          end
          job_log{num_tasks}.row = model;
          job_log{num_tasks}.col = fold;
          job_log{num_tasks}.task_no = num_tasks;
          num_tasks = num_tasks + 1;
        end
    end
    if verbose, t=toc(tid); fprintf('done (%d tasks, %.2f secs)\n', num_tasks-1, t); end    
    
    if distributed,
        % Set jobData (global variable to all tasks)    
        if verbose, fprintf('Set job data...'); tid=tic(); end    
        job_data.X = data.X;
        job_data.Y = data.Y;
        job_data.R = R;
        job_data.job_log = job_log;
        set(job, 'JobData', job_data);
        set(job, 'PathDependencies', strread(jobParam.path,'%s','delimiter',';'));
        if verbose, t=toc(tid); fprintf('done (%.2f secs)\n', t); end  

        % Submit and wait
        if verbose, fprintf('Submit and wait...'); tid=tic(); end    
        submit(job);  
    end
end

function params = assign_validate_values(params,values)
  if isfield(params, 'validate_params')
    for i=1:numel(params.validate_params)
        params = setfield(params,params.validate_params{i},values(i));
    end     
  end
end

function validateList = get_validate_list(params)
  validateList = [];
  if isfield(params, 'validate_params')
    switch numel(params.validate_params)
      case 1
          validateList = params.validate_values;
      case 2
          [a,b] = ndgrid(params.validate_values{:});
          validateList = [a(:) b(:)];
      case 3
          [a,b,c] = ndgrid(params.validate_values{:});
          validateList = [a(:) b(:) c(:)];
      case 4
          [a,b,c,d] = ndgrid(params.validate_values{:});
          validateList = [a(:) b(:) c(:) d(:)];
      case 5
          [a,b,c,d,e] = ndgrid(params.validate_values{:});
          validateList = [a(:) b(:) c(:) d(:) e(:)];
      otherwise
          error('Number of validate parameters cannot exceed 5\n');
    end
  end
end