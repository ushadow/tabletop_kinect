function stat = evalclassification(Y, R, evalfun)
% Args:
% - evalfun: evaluation function.
if iscell(R)
  nfold = size(R, 2);
  stat = cell(1, nfold);
  for i = 1 : nfold
    split1 = R{i}.split;
    Ytrue.train = Y(split1{1});
    Ytrue.validate = Y(split1{2});
    stat{i} = evalOneFold(Ytrue, R{i}.prediction, evalfun);
  end
else
  stat = evalOneFold(Y, R, evalfun);
end
end

function stat = evalOneFold(Y, R, evalfun)
stat = containers.Map();
stat('train') = evaluate(Y.train, R.train, evalfun);
stat('validate') = evaluate(Y.validate, R.validate, evalfun);
if isfield(Y, 'test')
  stat('test') = evaluate(Y.test, R.test, evalfun);
end
end

function r = evaluate(Ytrue, Ystar, evalfun)
%
% Args:
% - Ytrue: cell array of sequences.
total = 0;
nlabel = size(Ystar{1}, 1);
r = containers.Map();
error = zeros(nlabel, 1);
for i = 1 : length(Ytrue)
  error = error + evalfun(Ytrue{i}, Ystar{i});
  total = total + size(Ytrue{i}, 2);
end
r('error') = error / total;
end