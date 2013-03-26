function param = createahmmparam

% Node sizes.
param.nG = 6;
param.nF = 2;

param.nXhandFet = 3; % Number of hand features.

param.ss = 4; % slice size

% Node label
param.G1 = 1; 
param.S1 = 2; 
param.F1 = 3; 
param.X1 = 4;

param.onodes = [param.G1 param.F1 param.X1];

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
end