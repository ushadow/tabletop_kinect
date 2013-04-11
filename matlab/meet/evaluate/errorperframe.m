function error = errorperframe(Ytrue, Ystar)
nPredictedNode = size(Ystar, 1);
Ytrue = Ytrue(1 : nPredictedNode, :);
error = sum(1 - (cell2mat(Ytrue) == cell2mat(Ystar)), 2);
end
