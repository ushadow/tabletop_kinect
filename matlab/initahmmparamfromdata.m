function param = initahmmparamfromdata(param, mean)
  param.nS = size(mean, 2);
  param.nX = param.nXconFet + param.nXhandFet;
      
  % Parameters related to S.
  param.Sstartprob = ones(param.nG, param.nS) / param.nS;
  param.Stransprob = ones(param.nS, param.nG, param.nS) / param.nS;
  param.Stermprb = ones(param.nG, param.nS, param.nF) / param.nF;

  param.Xmean = mean;
  param.Xcov = repmat(eye(param.nX, param.nX) * 0.5, [1, 1, param.nS]);
end