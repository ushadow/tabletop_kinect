function params = init_model_params(data)

% Node sizes.
params.nG = 6;
params.nS = 4;
params.nF = 2;
params.nX = 11; % number of continuous features
params.hand_size = 2500;

params.ss = 4; % slice size

% Node label
params.G1 = 1; 
params.S1 = 2; 
params.F1 = 3; 
params.X1 = 4;

params.onodes = [params.G1 params.F1 params.X1];
params.Gstartprob = zeros(1, params.nG);
params.Gstartprob(1, 1) = 1;

params.Gtransprob = zeros(params.nG, params.nG);
delta = 0.05;
params.Gtransprob(1, 1 : params.nG - 1) = (1 - delta) / (params.nG - 1);
params.Gtransprob(1, params.nG) = delta;
params.Gtransprob(2, [1 3 : params.nG]) = delta / (params.nG - 1);
params.Gtransprob(2, 2) = 1 - delta;
params.Gtransprob(3 : params.nG, 1) = delta;
params.Gtransprob(3 : params.nG, 2 : params.nG) = ...
    (1 - delta) / (params.nG - 1);
end