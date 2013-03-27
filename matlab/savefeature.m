function savefeature(X, split, prefix, nhandFet, startHandFetNDX)
%
% Args
% - X: cell array of feature data.

for i = 1 : size(split, 2)
  train = X(split{1, i});
  result = eigenhand(train, nhandFet, startHandFetNDX);
  standardized = standardizefeature(result, true);
  nfeature = size(standardized, 1);
  assert(nfeature == nhandFet + startHandFetNDX - 1);
  filename = sprintf('%sfeature-%d-%d.csv', prefix, nfeature, i);
  csvwrite(filename, standardized');
end
end