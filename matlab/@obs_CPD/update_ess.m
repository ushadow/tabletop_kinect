function CPD = update_ess(CPD, fmarginal, evidence, ns, cnodes, ...
                          hiddent_bitv)
% Figure out the node numbers associated with the parent.
dom = fmarginal.domain;
self = dom(end);

% Discrete parent.
dp = dom(1 : end - 1);


end