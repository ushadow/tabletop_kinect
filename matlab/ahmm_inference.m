function [engine, ll] = ahmm_inference(params, evidence)

ahmm = createmodel(params);
engine = smoother_egine(jtree_2TBN_inf_egine(ahmm));
[engine, ll] = enter_evidence(engine, evidence);

end
