function R = inferenceahmm(ahmm, data, predictNode)

engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));

nseq = length(data);
R = cell(1, nseq);
for i = 1 : nseq
  evidence = data{i};
  engine = enter_evidence(engine, evidence);
  R{i} = mapest(engine, predictNode, length(evidence));
end
end
