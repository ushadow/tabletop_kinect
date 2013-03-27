function ahmm = createahmm(param)
% CREATEMODEL creates a graphical AHMM model.
% 
% ahmm = createmodel(nS)
% params: a structure of parameters for the model, including parameters for
%         different CPDs.
%   nX: number of continuous features.
%   Gstartprob: nG vector of start prob for G1. 
%   Sstartprob: nG X nS vector of start prob for S1.
%   hand: hand(:, :, i) is a depth image of the hand.
%
% Topology of the model:
% G1-->G2 
% |\   ^|\
% | v / | v
% | F1  | F2
% | ^   | ^
% |/    |/
% v     v
% S1-->S2
% |     |
% v     v
% X1    X2

% Topology
ss = 4; % Number of nodes in one time slice.

intra = zeros(ss);

% Has to be in the topological order.
G1 = param.G1; S1 = param.S1; F1 = param.F1; X1 = param.X1; 
G2 = G1 + ss; S2 = S1 + ss; F2 = F1; X2 = X1;

intra(G1, S1) = 1;
intra(S1, X1) = 1;
intra(G1, F1) = 1;
intra(S1, F1) = 1;

inter = zeros(ss);
inter(G1, G1) = 1;
inter(F1, G1) = 1;
inter(S1, S1) = 1;
 
node_sizes = [param.nG param.nS param.nF param.nX];
dnodes = [G1 S1 F1];
onodes = param.onodes;

% eclass1(i) is the equivalence class that node i in slice 1 belongs to. 
% eclass2(i) is the equivalence class that node i in slice 2, 3, ..., 
% belongs to.
eclass1 = 1 : ss;
eclass2 = [G2, S2, F2, X2];

ahmm = mk_dbn(intra, inter, node_sizes, 'discrete', dnodes, 'observed', ...
              onodes, 'eclass1', eclass1, 'eclass2', eclass2);

% Set CPD.
% Slice 1.
ahmm.CPD{G1} = tabular_CPD(ahmm, G1, param.Gstartprob);
ahmm.CPD{S1} = tabular_CPD(ahmm, S1, param.Sstartprob);

ahmm.CPD{F1} = tabular_CPD(ahmm, F1, param.Stermprob);

if isfield(param, 'hand')
  ahmm.CPD{X1} = obs_CPD(ahmm, X1, param.hand, param.hd_mu, ...
                         param.hd_sigma, 'mean', param.Xmean, 'cov', ...
                         param.Xcov);
else
  ahmm.CPD{X1} = gaussian_CPD(ahmm, X1, 'mean', param.Xmean, ...
      'cov', param.Xcov);
end

% Slice 2.
ahmm.CPD{G2} = hhmm2Q_CPD(ahmm, G2, 'Fself', [], 'Fbelow', F1, 'Qps', ...
                          [], 'startprob', param.Gstartprob, ...
                          'transprob', param.Gtransprob);
% Tablular CPD are stored as multidimentional arrays where the dimensions
% are arranged in the same order as the nodes. Nodes in the 2nd slice is 
% is after the ndoes in the 1st slice.
ahmm.CPD{S2} = tabular_CPD(ahmm, S2, param.Stransprob);
end