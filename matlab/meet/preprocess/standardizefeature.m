function standardized = standardizefeature(data, ~, varargin)
% Args
% - data: cell array
%
% Return
% - standardized: if reMat is true, returns a matrix, otherwise returns a 
%                 cell array.
ST = dbstack;
funname = ST.name;

retMat = false;
for i = 1 : 2 : length(varargin)
  switch varargin{i}
    case 'retMat'
      retMat = varargin{i + 1};
      logdebug(funname, 'retMat', retMat);
    otherwise
      error(['invalid optional argument' varargin{i}]);
  end
end

if isfield(data, 'train')
  train = data.train;
else
  train = data;
end

mat = data2mat(train); % Each column is a feature vector.
[matTrain mu sigma2] = standardize(mat);

if isfield(data, 'train')
  standardized.train = mat2data(matTrain, train);
else
  standardized = matTrain;
end

if isfield(data, 'validate')
  mat = data2mat(data.validate);
  standardized.validate = standardize(mat, mu, sigma2);
  if ~retMat
    standardized.validate = mat2data(standardized.validate, data.validate);
  end
end

if isfield(data, 'test')
  mat = data2mat(data.test);
  standardized.test = standardize(mat, mu, sigma2);
  if ~retMat
    standardized.test = mat2data(standardized.test, data.test);
  end
end
end

function data = mat2data(mat, data)
  ndx = 0;
  for i = 1 : length(data)
    for j = 1 : length(data{i})
      ndx = ndx + 1;
      data{i}{j} = mat(:, ndx);
    end
  end
end