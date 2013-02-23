function CPD = maximize_params(CPD, temp)

if ~adjustable_CPD(CPD), return; end

if CPD.clamped_mean
  cl_mean = CPD.mean;
else
  cl_mean = [];
end

if CPD.clamped_cov
  cl_cov = CPD.cov;
else
  cl_cov = [];
end

