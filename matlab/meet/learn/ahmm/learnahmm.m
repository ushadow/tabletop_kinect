function [R, finalAhmm] = learnahmm(Y, X, param)
% LEARNAHMMPARAM learns the parameters for the AHMM model.
%
% learn_ahmm_params(data)
% ahmm: the model.
% data: training data. A cell-array of 2D cell-arrays. For example,
% case{l}{i, t} is the value of node i in slice t in sequel l, or [] if
% unobserved.
nfeature = param.nconFet + param.nhandFet;
filename = sprintf('%s%s-feature-%d-%d-mean-%d.csv', param.dir, ...
                   param.userId, nfeature, param.fold, param.nS);
logdebug('learnahmm', 'read file', filename);
imported = importdata(filename, ',', 1);
mean = imported.data;

param = initahmmparam(param, mean);

trainData = createInputData(Y.train, X.train, param);
finalAhmm = trainahmm(param, trainData);

predictNode = [param.G1 param.F1];
param.onodes = [param.X1];
finalAhmm = sethiddenbit(finalAhmm, param.onodes);
checkahmm(finalAhmm);

trainData = createInputData(Y.train, X.train, param);
R.train = inferenceahmm(finalAhmm, trainData, predictNode, ...
                        param.inferMethod);

validateData = createInputData(Y.validate, X.validate, param);
R.validate = inferenceahmm(finalAhmm, validateData, predictNode, ...
                           param.inferMethod);
end

function data = createInputData(Y, X, param)
%
% Args:
% - Y: cell array of label data.
% - X: cell array of feature data.
data = cell(1, length(Y));
ss = param.ss;
for i = 1 : length(data)
  data{i} = cell(ss, length(Y{i}));
  if any(param.onodes == param.G1)
    data{i}(param.G1, :) = Y{i}(1, :);
  end
  if any(param.onodes == param.F1)
    data{i}(param.F1, :) = Y{i}(2, :);
  end
  data{i}(param.X1, :) = X{i};
end

if any(param.onodes == param.G1)
  assert(all(size(data{end}{param.G1, end}) == size(Y{end}{1, end})));
else
  assert(isempty(data{end}{param.G1, end}));
end
assert(isempty(data{end}{param.S1, end}));

if any(param.onodes == param.F1)
  assert(all(size(data{end}{param.F1, end}) == size(Y{end}{2, end})));
else
  assert(isempty(data{end}{param.F1, end}));
end

assert(all(size(data{end}{param.X1, end}) == [param.nX 1]));
end