function cvstat = evalcv(stat)
nfold = length(stat);
train = zeros(2, nfold);
validate = zeros(2, nfold);
if isfield(stat, 'test')
  test = zeros(2, 1);
end

for i = 1 : nfold
  train(:, i) =  stat{i}.train.error;
  validate(:, i) = stat{i}.validate.error;
  if isfield(stat, 'test')
    test(:, i) = stat{i}.test.error;
  end
end

cvstat.train.meanerror = mean(train, 2);
disp('training error mean:');
disp(cvstat.train.meanerror);

cvstat.train.stderror = std(train, 0, 2);
disp('training error std:');
disp(cvstat.train.stderror);

cvstat.validate.meanerror = mean(validate, 2);
disp('validation error mean:');
disp(cvstat.validate.meanerror);

cvstat.validate.stderror = std(validate, 0, 2);
disp('validation error std:');
disp(cvstat.validate.stderror);

if isfield(stat, 'test')
  cvstat.test.meanerror = mean(test, 2);
  cvstat.test.stderror = std(test, 0, 2);
end
end
