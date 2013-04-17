function runexperimentbatch(batch, modelParam, jobParam)
% RUNEXPERIMENTBATCH runs experiment for each batch and reports data.
%
% Args:
% - batch: cell array of data.

nBatch = length(batch);

% Run experiments.
ret = cell(1, nBatch);
for i = 1 : nBatch
  data = batch{i};
  ret{i} = runexperimentparallel(data, modelParam, jobParam);
end

% Report results.
fprintf('\n==========================================\n'); 
fprintf('SetName\tRand%%\tTrLev%%\tTrLen%%\tTeLev%%\tTeLen%%\tTime(s)\n');   

randScore = zeros(nBatch, 1);
trLevenScore = zeros(nBatch, 1);
trLengthScore = zeros(nBatch, 1);
vaLevenScore = zeros(nBatch, 1); % Validation score
vaLengthScore = zeros(nBatch, 1);
time = zeros(nBatch, 1);


for i = 1 : nBatch
  ret1 = ret{i};
  if isa(ret1, 'parallel.job.CJSIndependentJob')
    R = ret1.JobData.R;
    waitForState(ret1, 'finished');
    rows = cellfun(@(x) x.('row'), ret1.JobData.job_log);
    cols = cellfun(@(x) x.('col'), ret1.JobData.job_log);
    for r = unique(rows)
      for c = unique(cols)
        task = rows == r & cols == c;
        if verbose, fprintf('.'); end 
        if ~isempty(ret1.Tasks(task).OutputArguments)
          R{r,c} = ret1.Tasks(task).OutputArguments{1};
        end
      end
    end

    % Destroy job
    if jobParam.destroy,
      job.destroy();
    end
    ret1 = R;
  end
  
  data = batch{i};
  fprintf('%s\t', data.userId);
  % Baseline: error rate for random substitutions.
  randScore(i) = (1 - 1 / data.dataParam.vocabularySize);
  fprintf('%5.2f\t', 100 * randScore(i));
    
end