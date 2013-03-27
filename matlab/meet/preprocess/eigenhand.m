function [newX sortedEigVal eigHand] = eigenhand(X, neigHand, startHandFetNDX)
% EIGENHAND computes eigenhands and hand features based on the eigenhands.
%
% [eigHand handFeature rawFeature H] = eigenhand(X) 
% 
% X: observed data or a struct with train, validate and test data. Each 
%    data is a cell array and each cell is a feature vector.
% 
% Returns
% X: structure of train, validate, test data with eigenhand features.
% sortedEigVal: a vector of sorted eigenvalues correspoinding to the 
%               eigHand.
% eigHand: a npixel x K matrix where K is the number of eigenvectors chosen.
% handFeature: K x nframe matrix.
% rawFeature: all hand images in column vectors.

if isfield(X, 'train')
  train = X.train;
else
  train = X;
end

[normalizedFeature, mean] = normalizefeature(train, startHandFetNDX);

A = normalizedFeature;

% Let u be the eigenhand. We want to find AA' * u = lamda * u, but AA' is a 
% large matrix.

C = A' * A;
[eigMat, eigVal] = eig(C);

eigValVect = diag(eigVal);
[sortedEigVal, eigNDX] = sort(eigValVect, 'descend');
sortedEigMat = eigMat(:, eigNDX(1 : neigHand)); % nframe x neighenhand
sortedEigVal = sortedEigVal(1 : neigHand);

eigHand = normc(A * sortedEigMat); % npixel x neigenhand

newX.train = updatedata(train, eigHand, startHandFetNDX, ...
                     'normalized', normalizedFeature);

if isfield(X, 'validate')
  newX.validate = updatedata(X.validate, eigHand, startHandFetNDX, ...
                          'mean', mean);
end

if isfield(X, 'test')
  newX.test = updatedata(X.test, eigHand, startHandFetNDX, 'mean', mean);
end

npixel = size(normalizedFeature, 1);
assert(all(size(eigHand) == [npixel neigHand]));
assert(all(size(newX.train{end}{end}) == ...
                [startHandFetNDX - 1 + neigHand, 1]));
assert(abs(norm(eigHand(:, 1)) - 1) < 1e-9);
end
  
function rawFeature = rawhandfeature(data, startHandFetNDX)
mat = data2mat(data);
rawFeature = mat(startHandFetNDX : end, :);
end

function [normalized meanFeature] = normalizefeature(data, startHandFetNDX)
rawFeature = rawhandfeature(data, startHandFetNDX);
nframe = size(rawFeature, 2);
meanFeature = mean(rawFeature, 2);
meanFeatureRep = repmat(meanFeature, 1, nframe);
normalized = rawFeature - meanFeatureRep;
end

function data = updatedata(data, eigHand, startHandFetNDX, varargin)
narg = length(varargin);
for i = 1 : 2 : narg
  switch varargin{i}
    case 'mean' 
      rawFeature = rawhandfeature(data, startHandFetNDX);
      nframe = size(rawFeature, 2);
      meanFeature = varargin{i + 1};
      meanFeatureRep = repmat(meanFeature, 1, nframe);
      normalizedFeature = rawFeature - meanFeatureRep;
    case 'normalized', normalizedFeature = varargin{i + 1};
    otherwise, error(['invalid argument name ' varargin{i}]);
  end
end
handFeature = eigHand' * normalizedFeature; % neigHand x nframe
nseq = length(data);
ndx = 0;
for i = 1 : nseq
  for t = 1 : length(data{i})
    ndx = ndx + 1;
    old = data{i}{t};
    data{i}{t} = [old(1 : startHandFetNDX - 1); handFeature(:, ndx)]; 
  end
end
neigHand = size(eigHand, 2);
nframe = size(normalizedFeature, 2);
assert(all(size(handFeature) == [neigHand nframe]));
assert(all(data{end}{end}(end - neigHand + 1 : end) ...
                          == handFeature(1 : neigHand, end)));
end