function finalAhmm = trainahmm(param, data)
% Arg
% - data: cell array of training data.
nfeature = param.nXconFet + nXhandFet;
filename = sprintf('%sfeature-%d-mean-%d.csv', param.dir, nfeature, ...
                   param.fold); 
mean = importdata(filename, ',', 1);

param = initahmmparam(param, mean);

ahmm = createahmm(param);

engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));

finalAhmm = learn_params_dbn_em(engine, data, 'max_iter', param.maxIter);
end