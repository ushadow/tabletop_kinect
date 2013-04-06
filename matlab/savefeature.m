function denoised = savefeature(X, split, hyperParam)
% SAVEFEATURE saves the standardized features.
%
% Args
% - X: cell array of feature data.
% - prefix: directory to save the features.

denoised = denoise(X, hyperParam);
nfet = hyperParam.nhandFet + hyperParam.startHandFetNDX - 1;
for i = 1 : size(split, 2)
  train = denoised(split{1, i});
  result = eigenhand(train, hyperParam);
  assert(length(result{1}{1}) == nfet);
  standardized = standardizefeature(result, 0, 'retMat', true);
  nfeature = size(standardized, 1);
  assert(nfeature == hyperParam.nhandFet + hyperParam.startHandFetNDX - 1);
  filename = sprintf('%sfeature-%d-%d.csv', hyperParam.dir, nfeature, i);
  csvwrite(filename, standardized');
end
end