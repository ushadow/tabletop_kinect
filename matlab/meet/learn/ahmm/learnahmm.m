function R = learnahmm(Y, X, param)
% LEARNAHMMPARAM learns the parameters for the AHMM model.
%
% learn_ahmm_params(data)
% ahmm: the model.
% data: training data. A cell-array of 2D cell-arrays. For example,
% case{l}{i, t} is the value of node i in slice t in sequel l, or [] if
% unobserved.

trainData = createInputData(Y.train, X.train, param);
finalAhmm = trainahmm(param, trainData);

predictNode = [param.G1 param.F1];
R.train = inferenceahmm(finalAhmm, trainData, predictNode);

param.onodes = [param.X1];
validateData = createInputData(Y.validate, X.validate, param);
R.validate = inferenceahmm(finalAhmm, validateData, predictNode);
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
  if ~isempty(param.onodes == param.G1)
    data{i}{param.G1, :} = Y{i}{1, :};
  end
  if ~isemtpy(param.onodes == param.F1)
    data{i}{param.F1, :} = Y{i}{2, :};
  end
  data{i}{param.X1, :} = X{i};
end
end