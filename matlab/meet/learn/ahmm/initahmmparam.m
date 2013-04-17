function param = initahmmparam(param, mean)
% INITAHMMPARAM initializes AHMM parameters.
%
% Arg
% - param: struct with the following fields
%   -- fold: the current fold.

param.nG = param.vocabularySize;
param.nF = 2;
param.nX = param.nconFet + param.nhandFet;
assert(all(size(mean) == [param.nX param.nS]));

param.ss = 4; % slice size

% Node label
param.G1 = 1; 
param.S1 = 2; 
param.F1 = 3; 
param.X1 = 4;

param.onodes = [param.G1 param.X1];
% Parameters related to G.
param.Gstartprob = zeros(1, param.nG);
param.Gstartprob(1, 1) = 1;

param.Gtransprob = zeros(param.nG, param.nG);
delta = 0.05;
param.Gtransprob(1, 1 : param.nG - 1) = (1 - delta) / (param.nG - 1);
param.Gtransprob(1, param.nG) = delta;
param.Gtransprob(2, [1 3 : param.nG]) = delta / (param.nG - 1);
param.Gtransprob(2, 2) = 1 - delta;
param.Gtransprob(3 : param.nG, 1) = delta;
param.Gtransprob(3 : param.nG, 2 : param.nG) = ...
    (1 - delta) / (param.nG - 1);
  
% Parameters related to S. Uniform initialization.
param.Sstartprob = ones(param.nG, param.nS) / param.nS;
param.Stransprob = ones(param.nS, param.nG, param.nS) / param.nS;
param.Stermprob = ones(param.nG, param.nS, param.nF) / param.nF;

param.Xmean = mean;
param.Xcov = repmat(eye(param.nX, param.nX), [1, 1, param.nS]);
param.Xcov_type = param.cov_type;
end