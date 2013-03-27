function savefeature(X, split, prefix, neigHand, npixel)
nfeature = length(X{1}{1});
for i = 1 : size(split, 2)
  train = X(split{1, i});
  result = eigenHand(train, neigHand, npixel);
  train = result.train;
  for j = 1 : length(train)
    train{j} = cell2mat(train{j});
  end
  all = cell2mat(train);
  filename = sprintf('%sfeature-%d-%d.csv', prefix, nfeature, i);
  csvwrite(filename, all');
end
end