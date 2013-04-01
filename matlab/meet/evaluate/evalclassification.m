function stat = evalclassification(Y, R)
if iscell(R)
  nfold = size(R, 2);
  stat = cell(1, nfold);
  for i = 1 : nfold
    split1 = R{i}.split;
    Ytrue.train = Y(split1{1});
    Ytrue.validate = Y(split1{2});
    stat{i} = evalOneFold(Ytrue, R{i}.prediction);
  end
else
  stat = evalOneFold(Y, R);
end
end

function stat = evalOneFold(Y, R)
stat.train = evaluate(Y.train, R.train);
stat.validate = evaluate(Y.validate, R.validate);
if isfield(Y, 'test')
  stat.test = evaluate(Y.test, R.test);
end
end

function r = evaluate(Ytrue, Ystar)
%
% Args:
% - Ytrue: cell array of sequences.
total = 0;
nlabel = size(Ytrue{1}, 1);
r.error = zeros(nlabel, 1);
for i = 1 : length(Ytrue)
  for j = 1 : size(Ytrue{i}, 2)
    total = total + 1;
    for k = 1 : nlabel
      if Ytrue{i}{k, j} ~= Ystar{i}{k, j};
        r.error(k) = r.error(k) + 1;
      end
    end
  end
end
r.error = r.error / total;
end