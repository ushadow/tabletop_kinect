function R = inferenceahmm(ahmm, data, predictNode, method)

switch method
  case 'fixed-interval-smoothing'
  case 'viterbi'
    engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));
  case 'filtering'
    engine = filter_engine(jtree_2TBN_inf_engine(ahmm));
  otherwise
    error(['Inference method not implemented: ' method]);
end

nseq = length(data);
R = cell(1, nseq);
for i = 1 : nseq
  evidence = data{i};
  switch method
    case 'fixed-interval-smoothing'
      engine = enter_evidence(engine, evidence);
      R{i} = mapest(engine, predictNode, length(evidence));
    case 'filtering'
      T = size(evidence, 2);
      nhnode = length(predictNode);
      mapEst = cell(nhnode, T);
      for t = 1 : T
        for n = 1 : nhnode
          engine = enter_evidence(engine, evidence, t);
          m = marginal_nodes(engine, predictNode, t);
          [~, ndx] = max(m.T);
          mapEst{n, t} = ndx;
        end
      end
      R{i} = mapEst;
    case 'viterbi'
      % Find the most probable explanation (Viterbi).
      mpe = find_mpe(engine, evidence);
      R{i} = mpe(predictNode, :);
    otherwise
      error(['Inference method not implemented: ' method]);
  end
end
end
