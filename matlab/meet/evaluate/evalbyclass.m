function stat = evalbyclass(Y, R, cat)
% EVALCONTDISC evaludates continuous vs discrete gesture classification.
% Args:
% - R: cell array

nfold = size(R, 2);
stat = cell(1, nfold);
for i = 1 : nfold
  split1 = R{i}.split;
  Ytrue.train = Y(split1{1});
  Ytrue.validate = Y(split1{2});
  stat{i} = evalOneFold(Ytrue, R{i}.prediction, cat);
end

end

function stat = evalOneFold(Y, R, cat)
key = {'train', 'validate'};
value = {evaluate(Y.train, R.train, cat), ...
         evaluate(Y.validate, R.validate, cat)};
stat = containers.Map(key, value);
if isfield(Y, 'test')
  stat('test') = evaluate(Y.test, R.test, cat);
end
end

function stat = evaluate(Ytrue, Ystar, cat)
nlabel = size(Ystar{1}, 1);
totalCount = zeros(1, 4); % [tp fp tn fn]
for i = 1 : length(Ytrue)
  for j = 1 : size(Ytrue{i}, 2)
    for k = 1 : nlabel
      count = quantify(Ytrue{i}{k, j}, Ystar{i}{k, j}, cat);
      totalCount = totalCount + count;
    end
  end
end

key = {'precision', 'recall', 'accuracy'};
value = {totalCount(1) / sum(totalCount([1 2])), ...
         totalCount(1) / sum(totalCount([1 4])), ...
         sum(totalCount([1 3])) / sum(totalCount)};
stat = containers.Map(key, value);
end