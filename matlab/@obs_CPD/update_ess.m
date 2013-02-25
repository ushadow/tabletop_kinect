function CPD = update_ess(CPD, fmarginal, evidence, ns, cnodes, ...
                          hiddent_bitv)
% fmarginal: the marginal on self's family which includes self's parents
% and self.
% evidence: 2D cell array for one sequence.
                        
% Figure out the node numbers associated with the parent. The node number
% is unrolled in time, i.e. i + (t - 1) * ss.
dom = fmarginal.domain;
self = dom(end);

CPD.nsamples = CPD.nsamples + 1;
[ss dpsz] = size(CPD.mean); % ss = self size

w = fmarginal.T(:);
y = evidence{self};
Cyy = y * y';
WY = repmat(w(:)', ss, 1); % WY(y, i) = w(i)
WYY = repmat(reshape(WY, [ss 1 dpsz]), [1 ss 1]); % WYY((y, y', i) = w(i)
CPD.WYsum = CPD.WYsum + y(:) * w(:)'; % w(:)' is a row matrix.
CPD.WYYsum = CPD.WYYsum + ...
             WYY .* repmat(reshape(Cyy, [ss ss 1]), [1 1 dpsz]);
end