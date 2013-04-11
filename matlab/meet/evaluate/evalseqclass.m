function stat = evalseqclass(Ytrue, Ystar)
% EVALSEQCLASS evaluates the classification as a sequence not by frame.
% Args:
% - Ytrue: a vector.
% - Ystar: a vector.
trueLabel = unique(Ytrue);
actualLabel = unqiue(Ystar);
stat = edit_distance_levenshtein(trueLabel, actualLabel);
end