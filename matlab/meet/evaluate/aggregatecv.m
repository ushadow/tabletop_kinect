function cvstat = aggregatecv(R)
% 
% Args:
% - R: cell array.
ST = dbstack;
fname = ST.name;

nfold = length(R);

memo = containers.Map();

for f = 1 : nfold
  memo = computeonefold(R{f}, memo, nfold, f);
end

key = keys(memo);
cvstat = containers.Map();
for i = 1 : length(key)
  datatype = key{i};
  logdebug(fname, 'datatype', datatype);
  quantity = memo(datatype);
  quantname = keys(quantity);
  aggregate = containers.Map();
  for j = 1 : length(quantname)
    name = quantname{j};
    aggrname = [name '-mean'];
    aggregate(aggrname) = ignoreNaN(quantity(name), @mean, 2);
    logdebug(fname, aggrname, aggregate(aggrname));
    aggrname = [name '-std'];
    aggregate(aggrname) = ignoreNaN(quantity(name), @std, 2);
    logdebug(fname, aggrname, aggregate(aggrname));
  end
  
  if (isKey(aggregate, 'precision-mean')) && ...
     (isKey(aggregate, 'recall-mean'))
    precision = aggregate('precision-mean');
    recall = aggregate('recall-mean');
    fscore = 2 * precision * recall / (precision + recall);
    aggregate('fscore') = fscore;
    logdebug(fname, 'fscore', fscore);
  end
  cvstat(datatype) = aggregate;
end
end

function memo = computeonefold(r, memo, nfold, index)
%
% Args:
% - r: stats for one fold.
if isfield(r, 'stat');
  stat = r.stat;
else
  stat = r;
end
key = keys(stat);
nkey = length(key);

for i = 1 : nkey
  datatype = key{i};
  subkey = keys(stat(datatype));
  subvalue = values(stat(datatype));
  nsubkey = length(subkey);
  nvar = size(subvalue{1}, 1);
  
  if isKey(memo, datatype)
    quantity = memo(datatype);
  else
    value = cell(nsubkey, 1);
    for j = 1 : nsubkey
      value{j} = zeros(nvar, nfold);
    end
    quantity = containers.Map(subkey, value);
  end
  
  for j = 1 : nsubkey
    quantname = subkey{j}; 
    value = quantity(quantname);
    value(:, index) = subvalue{j};
    quantity(quantname) = value;
  end
  
  memo(datatype) = quantity;
end
end
