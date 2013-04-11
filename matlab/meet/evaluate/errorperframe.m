function error = errorperframe(Ytrue, Ystar)
error = sum(1 - (cell2mat(Ytrue) == cell2mat(Ystar)), 2);
end
