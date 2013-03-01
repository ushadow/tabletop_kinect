clear params;

params.nG = 2;
params.nS = 4;
params.nF = 2;
params.nX = 2;

params.Gstartprob = [0.5 0.5];
params.Gtransprob = [0.5 0.5;
                     0.5 0.5];
                   
% 2 x 4 matrix                   
params.Sstartprob = [0.5 0.5 0 0
                     0.5 0 0.5 0];
AS = zeros(params.nS, params.nG, params.nS + 1);
AS(:, 1, :) = [0 1 0 0 0
               0 0 0 1 0
               0 0 0 0 1
               0.5 0 0 0 0.5];
AS(:, 2, :) = [0 0 1 0 0
               0 0 0 0 1
               0 0 0 1 0
               0.5 0 0 0 0.5];
               
[params.Stransprob, params.Stermprob] = remove_hhmm_end_state(AS);

n = 4;
params.hand = zeros(n, n, params.nS);
for i = 1 : params.nS
  params.hand(:, :, i) = repmat(i, n);
end

params.hd_mu = n * n * 0.5;
params.hd_sigma = 1;

params.Xmean = reshape(1 : 2 * n, [params.nX params.nS])';
params.Xcov = repmat(eye(params.nX), [1, 1, params.nS]);

ahmm = createmodel(params);

evidence = sample_dbn(ahmm, 10);