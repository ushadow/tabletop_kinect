function error = errorperframe(Ytrue, Ystar)
error = sum(1 - (Ytrue == Ystar), 2);
end
