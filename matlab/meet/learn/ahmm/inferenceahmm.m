function R = inferenceahmm(ahmm, data, predictNode)

engine = smoother_egine(jtree_2TBN_inf_egine(ahmm));

nseq = length(data);
R = cell(1, nseq);
for i = 1 : nseq
  evidence = data{i};
  engine = enter_evidence(engine, evidence);
  R{i} = mapest(engine, predictNode, length(evidence));
end
end
