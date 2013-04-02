function stat = evalclassification(Y, R, loss)
% Args:
% - loss: loss function.
if iscell(R)
  nfold = size(R, 2);
  stat = cell(1, nfold);
  for i = 1 : nfold
    split1 = R{i}.split;
    Ytrue.train = Y(split1{1});
    Ytrue.validate = Y(split1{2});
    stat{i} = evalOneFold(Ytrue, R{i}.prediction, loss);
  end
else
  stat = evalOneFold(Y, R, loss);
end
end

function stat = evalOneFold(Y, R, loss)
stat = containers.Map();
stat('train') = evaluate(Y.train, R.train, loss);
stat('validate') = evaluate(Y.validate, R.validate, loss);
if isfield(Y, 'test')
  stat('test') = evaluate(Y.test, R.test, loss);
end
end

function r = evaluate(Ytrue, Ystar, loss)
%
% Args:
% - Ytrue: cell array of sequences.
total = 0;
nlabel = size(Ystar{1}, 1);
r = containers.Map();
error = zeros(nlabel, 1);
for i = 1 : length(Ytrue)
  for j = 1 : size(Ytrue{i}, 2)
    total = total + 1;
    for k = 1 : nlabel
      error(k) = error(k) + loss(Ytrue{i}{k, j}, Ystar{i}{k, j});
    end
  end
end
r('error') = error / total;
end