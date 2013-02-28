function ahmm = createmodel(nS, params)
% CREATEMODEL creates a graphical AHMM model.
% 
% ahmm = createmodel(nS)
% nS: number of hidden states for S.
% params: a structure of parameters for different CPDs.
%
% Topology of the model:
% G1->G2 
% |\ / |\
% | F1 | F2
% |/   |/
% S1->S2
% |    |
% X1  X2

ss = 4; % Number of nodes in one time slice.
nG = 5; % Select, manipulative, stop, point, come back.
nX = 11; % Number of continuous features.
nF = 2; % 0, 1

intra = zeros(ss);
G1 = 1; S1 = 2; F1 = 3; X1 = 4; 
G2 = G1 + ss; S2 = S1 + ss; F2 = F1; X2 = X1; 
intra(G1, S1) = 1;
intra(S1, X1) = 1;
intra(G1, F1) = 1;
intra(S1, F1) = 1;

inter = zeros(ss);
inter(G1, G1) = 1;
inter(F1, G1) = 1;
inter(S1, S1) = 1;
 
nodeSizes = [nG nS nF nX];
dnodes = [G1 S1 F1];
onodes = X1;

% eclass1(i) is the equivalence class that node i in slice 1 belongs to. 
% eclass2(i) is the equivalence class that node i in slice 2, 3, ..., 
% belongs to.
eclass1 = 1 : ss;
eclass2 = [G2, S2, F2, X2];

ahmm = mk_dbn(intra, inter, nodeSizes, 'discrete', dnodes, 'observed', ...
              onodes, 'eclass1', eclass1, 'eclass2', eclass2);

% Set CPD.
ahmm.CPD{G1} = tabular_CPD(ahmm, G1, params.G1);
ahmm.CPD{S1} = tabular_CPD(ahmm, S1, params.S1);

% TODO: Check whether it is necessary to use hhmmF_CPD because it always 
% assumes F is hidden.
if isfield(params, 'F1')
    ahmm.CPD{F1} = hhmmF_CPD(ahmm, F1, S1, [], 'Qps', G1, 'termprob', ...
                             params.F1);
else
    ahmm.CPD{F1} = hhmmF_CPD(ahmm, F1, S1, [], 'Qps', G1);
end
%ahmm.CPD{X1} = 
ahmm.CPD{G2} = hhmm2Q_CPD(ahmm, G2, 'Fself', [], 'Fbelow', F1, 'Qps', ...
                          [], 'startprob', params.G2startprob, ...
                          'transprob', params.G2transprob);
ahmm.CPD{S2} = tabular_CPD(ahmm, S2, params.S2);
end