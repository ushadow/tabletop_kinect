function R = inferenceahmm(ahmm, data, predictNode, method)

engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));

nseq = length(data);
R = cell(1, nseq);
for i = 1 : nseq
  evidence = data{i};
  switch method
    case 'fixed-interval-smoothing'
      engine = enter_evidence(engine, evidence);
      R{i} = mapest(engine, predictNode, length(evidence));
    case 'viterbi'
      % Find the most probable explanation (Viterbi).
      mpe = find_mpe(engine, evidence);
      R{i} = mpe(predictNode, :);
    otherwise
      error(['Inference method not implemented: ' method]);
  end
end
end
