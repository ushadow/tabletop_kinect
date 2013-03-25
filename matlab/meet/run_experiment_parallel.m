function R = run_experiment_parallel( data, model_params, job_params )
     
    verbose = job_params.verbose;
    
    switch job_params.type
        case 'local'
            jm = findResource('scheduler','type','local');
            job = createJob(jm);
            distributed = true;
        case 'none'
            distributed = false;
    end
    
    
    R = cell(numel(model_params),size(data.split,2));    
    nargout = 1; % to be returned from each task 
 
    % Generate tasks
    if verbose, fprintf('Generate tasks'); tid=tic(); end
    num_tasks = 1;
    for model=1:numel(model_params) % for each model (row)
        params = model_params{model};
        validate_list = get_validate_list(params);
        for fold=1:size(data.split,2) % for each fold (col)
            for i=1:size(validate_list,1)
                if verbose, fprintf('.'); end
                params = assign_validate_values(params,validate_list(i,:));   
                if distributed
                    createTask(job,@run_experiment,nargout,{params,data.split(:,fold)});
                else
                    R{model,fold}{end+1} = run_experiment(params,data.split(:,fold),data);
                end
                job_log{num_tasks}.row = model;
                job_log{num_tasks}.col = fold;
                job_log{num_tasks}.task_no = num_tasks;
                num_tasks = num_tasks + 1;
            end
        end
    end
    if verbose, t=toc(tid); fprintf('done (%d tasks, %.2f secs)\n', num_tasks-1, t); end    
    
    if distributed,
        % Set jobData (global variable to all tasks)    
        if verbose, fprintf('Set job data...'); tid=tic(); end    
        job_data.X = data.X;
        job_data.Y = data.Y;
        job_data.job_log = job_log;
        set(job, 'JobData', job_data);
        set(job, 'PathDependencies', strread(job_params.path,'%s','delimiter',';'));
        if verbose, t=toc(tid); fprintf('done (%.2f secs)\n', t); end  


        % Submit and wait
        if verbose, fprintf('Submit and wait...'); tid=tic(); end    
        submit(job);
        waitForState(job,'finished');    
        if verbose, t=toc(tid); fprintf('done (%.2f secs)\n', t); end   

        % Collect results
        if verbose, fprintf('Collect results'); tid=tic(); end    
        rows = cellfun(@(x) getfield(x,'row'), job.JobData.job_log);
        cols = cellfun(@(x) getfield(x,'col'), job.JobData.job_log);
        for r=unique(rows)
            for c=unique(cols)
                tasks = find(rows==r & cols==c);
                R{r,c} = cell(1,numel(tasks));
                for t=1:numel(tasks)
                    if verbose, fprintf('.'); end 
                    if ~isempty(job.Tasks(tasks(t)).OutputArguments)
                        R{r,c}{t} = job.Tasks(tasks(t)).OutputArguments{1};
                    end
                end
            end
        end
        if verbose, t=toc(tid); fprintf('done (%.2f secs)\n', t); end
        
        % Destroy job
        if job_params.destroy,
            job.destroy();
        end
    end
end

function params = assign_validate_values(params,values)
    for i=1:numel(params.validate_params)
        params = setfield(params,params.validate_params{i},values(i));
    end     
end

function validate_list = get_validate_list(params)
    switch numel(params.validate_params)
        case 1
            validate_list = params.validate_values;
        case 2
            [a,b] = ndgrid(params.validate_values{:});
            validate_list = [a(:) b(:)];
        case 3
            [a,b,c] = ndgrid(params.validate_values{:});
            validate_list = [a(:) b(:) c(:)];
        case 4
            [a,b,c,d] = ndgrid(params.validate_values{:});
            validate_list = [a(:) b(:) c(:) d(:)];
        case 5
            [a,b,c,d,e] = ndgrid(params.validate_values{:});
            validate_list = [a(:) b(:) c(:) d(:) e(:)];
        otherwise
            error('Number of validate parameters cannot exceed 5\n');
    end
end